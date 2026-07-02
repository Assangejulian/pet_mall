package com.pat.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.product.domain.dto.ProductCreateDTO;
import com.pat.product.domain.dto.ProductQueryDTO;
import com.pat.product.domain.dto.ProductUpdateDTO;
import com.pat.product.domain.entity.Product;
import com.pat.product.mapper.ProductMapper;
import com.pat.product.mapper.ProductStoreLookupMapper;
import com.pat.product.service.ProductService;
import com.pat.product.domain.vo.ProductPageVO;
import com.pat.product.domain.vo.ProductStoreVO;
import com.pat.product.domain.vo.ProductVO;
import com.pat.store.domain.entity.Store;
import com.pat.store.mapper.StoreMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private static final int TYPE_PET = 1;
    private static final int TYPE_GOODS = 2;
    private static final int STATUS_OFFLINE = 0;
    private static final int STATUS_ONLINE = 1;
    private static final int STATUS_SOLD = 2;

    private final ProductStoreLookupMapper productStoreLookupMapper;
    private final StoreMapper storeMapper;
    private final ObjectMapper objectMapper;

    public ProductServiceImpl(ProductStoreLookupMapper productStoreLookupMapper, StoreMapper storeMapper, ObjectMapper objectMapper) {
        this.productStoreLookupMapper = productStoreLookupMapper;
        this.storeMapper = storeMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Product getForUpdate(Long id) {
        return baseMapper.selectForUpdateById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO createProduct(ProductCreateDTO dto) {
        // 新增商品只允许放到营业中的商店。
        // 管理员只能设置上架/下架，已售出状态由订单流程控制。
        validateCreateRequired(dto);
        checkOperatingStore(dto.getStoreId());
        Integer status = parseEditableStatus(dto.getStatus(), STATUS_ONLINE);
        String images = normalizeImages(dto.getImages());
        validateBusinessRules(dto.getProductType(), dto.getStock(), dto.getPrice(), status, false);

        Product product = new Product();
        product.setStoreId(dto.getStoreId());
        product.setProductName(dto.getProductName());
        product.setProductType(dto.getProductType());
        product.setCategory(dto.getCategory());
        product.setProductDesc(dto.getProductDesc());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setMainImage(dto.getMainImage());
        product.setImages(images);
        product.setStatus(status);
        product.setDeleted(0);

        if (!save(product)) {
            throw new BusinessException(ErrorCode.SAVE_FAILED, "商品新增失败");
        }
        return toVO(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO updateProduct(Long id, ProductUpdateDTO dto) {
        // 普通编辑支持部分字段更新。
        // 如果商品已经售出，身份字段和售出状态会被保留。
        Product oldProduct = getActiveProduct(id);
        if (dto != null && requestsOnline(dto.getStatus())) {
            ensureNotPlatformRestricted(oldProduct);
        }
        boolean soldProduct = oldProduct != null && oldProduct.getStatus() == STATUS_SOLD;
        boolean soldPet = soldProduct && oldProduct.getProductType() == TYPE_PET;
        Long storeId = soldProduct ? oldProduct.getStoreId() : (dto.getStoreId() != null ? dto.getStoreId() : oldProduct.getStoreId());
        if (!soldProduct && !storeId.equals(oldProduct.getStoreId())) {
            checkOperatingStore(storeId);
        }

        Integer productType = (soldPet || dto.getProductType() == null) ? oldProduct.getProductType() : dto.getProductType();
        Integer stock = soldPet ? 0 : (dto.getStock() == null ? oldProduct.getStock() : dto.getStock());
        BigDecimal price = dto.getPrice() == null ? oldProduct.getPrice() : dto.getPrice();
        Integer status = soldProduct ? STATUS_SOLD : parseEditableStatus(dto.getStatus(), oldProduct.getStatus());
        String images = dto.getImages() == null ? oldProduct.getImages() : normalizeImages(dto.getImages());
        validateBusinessRules(productType, stock, price, status, soldProduct);

        Product product = new Product();
        product.setId(id);
        product.setStoreId(storeId);
        product.setProductName(StringUtils.hasText(dto.getProductName()) ? dto.getProductName() : oldProduct.getProductName());
        product.setProductType(productType);
        product.setCategory(dto.getCategory() == null ? oldProduct.getCategory() : dto.getCategory());
        product.setProductDesc(dto.getProductDesc() == null ? oldProduct.getProductDesc() : dto.getProductDesc());
        product.setPrice(price);
        product.setStock(stock);
        product.setMainImage(dto.getMainImage() == null ? oldProduct.getMainImage() : dto.getMainImage());
        product.setImages(images);
        product.setStatus(status);
        product.setDeleted(null);
        product.setCreateTime(null);
        // videoId 当前由视频模块维护，商品编辑只保留原值。
        product.setVideoId(oldProduct.getVideoId());

        if (!updateById(product)) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "商品修改失败");
        }
        return toVO(getActiveProduct(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteProduct(Long id) {
        getActiveProduct(id);
        if (!removeById(id)) {
            throw new BusinessException(ErrorCode.DELETE_FAILED, "商品删除失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO onlineProduct(Long id) {
        // 上架使用原子条件更新，避免并发时把库存为0或已售出的商品上架。
        if (id == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商品ID不能为空");
        }
        Product current = getActiveProduct(id);
        ensureNotPlatformRestricted(current);
        checkOperatingStore(current.getStoreId());
        int rows = baseMapper.update(null, new LambdaUpdateWrapper<Product>()
                .set(Product::getStatus, STATUS_ONLINE)
                .eq(Product::getId, id)
                .eq(Product::getDeleted, 0)
                .isNull(Product::getOfflineReason)
                .isNull(Product::getOfflineUserId)
                .isNull(Product::getOfflineTime)
                .gt(Product::getStock, 0)
                .ne(Product::getStatus, STATUS_SOLD)
                .ne(Product::getStatus, STATUS_ONLINE));
        if (rows == 1) {
            return toVO(getActiveProduct(id));
        }
        throwOnlineFailure(id);
        throw new BusinessException(ErrorCode.UPDATE_FAILED, "商品上架失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO offlineProduct(Long id) {
        // 已售出商品不能再手动切回下架，售出状态由订单流程控制。
        Product product = getActiveProduct(id);
        if (product != null && product.getStatus() == STATUS_SOLD) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "已售出的商品不能改为下架");
        }
        Product update = new Product();
        update.setId(id);
        update.setStatus(STATUS_OFFLINE);
        if (!updateById(update)) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "商品下架失败");
        }
        return toVO(getActiveProduct(id));
    }

    @Override
    public IPage<ProductVO> pagePublicProducts(ProductQueryDTO query) {
        // 公开列表只展示已上架商品，支持用户端和管理端兼容查询参数。
        long pageNum = query.getPage() != null ? query.getPage() : (query.getPageNum() == null ? 1L : query.getPageNum());
        long pageSize = resolvePageSize(query);
        String keyword = resolveKeyword(query);
        Integer productType = resolveProductType(query);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, STATUS_ONLINE)
                .inSql(Product::getStoreId, "SELECT id FROM store WHERE deleted = 0 AND status = 1")
                .like(StringUtils.hasText(keyword), Product::getProductName, keyword)
                .eq(query.getStoreId() != null, Product::getStoreId, query.getStoreId())
                .eq(productType != null, Product::getProductType, productType)
                .eq(StringUtils.hasText(query.getCategory()), Product::getCategory, query.getCategory())
                .orderByDesc(Product::getCreateTime);

        Page<Product> page = page(new Page<>(pageNum, pageSize), wrapper);
        return convertPage(page);
    }

    @Override
    public ProductPageVO pageAdminProducts(ProductQueryDTO query) {
        // 管理端列表不隐藏下架/已售出商品，方便后台查看和维护。
        long pageNum = query.getPage() != null ? query.getPage() : (query.getPageNum() == null ? 1L : query.getPageNum());
        long pageSize = resolvePageSize(query);
        String keyword = resolveKeyword(query);
        Integer productType = resolveProductType(query);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .like(StringUtils.hasText(keyword), Product::getProductName, keyword)
                .eq(query.getStoreId() != null, Product::getStoreId, query.getStoreId())
                .eq(productType != null, Product::getProductType, productType)
                .eq(StringUtils.hasText(query.getCategory()), Product::getCategory, query.getCategory())
                .eq(query.getStatus() != null, Product::getStatus, query.getStatus())
                .orderByDesc(Product::getCreateTime);

        Page<Product> result = page(new Page<>(pageNum, pageSize), wrapper);
        Map<Long, Store> storeMap = loadStoreMap(result.getRecords());
        return new ProductPageVO(result.getRecords().stream().map(product -> toVO(product, storeMap)).toList(),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public ProductVO getPublicDetail(Long id) {
        Product product = getOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id)
                .eq(Product::getStatus, STATUS_ONLINE)
                .inSql(Product::getStoreId, "SELECT id FROM store WHERE deleted = 0 AND status = 1"), false);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在或已下架");
        }
        return toVO(product);
    }

    @Override
    public ProductVO getAdminDetail(Long id) {
        return toVO(getActiveProduct(id));
    }

    @Override
    public IPage<ProductVO> pageMerchantProducts(ProductQueryDTO query, Long merchantUserId) {
        if (merchantUserId == null) {
            throw new BusinessException(ErrorCode.NOT_AUTH, "未获取到当前商家");
        }
        long pageNum = query.getPage() != null ? query.getPage() : (query.getPageNum() == null ? 1L : query.getPageNum());
        long pageSize = resolvePageSize(query);
        String keyword = resolveKeyword(query);
        Integer productType = resolveProductType(query);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .inSql(Product::getStoreId, "SELECT id FROM store WHERE deleted = 0 AND user_id = " + merchantUserId)
                .like(StringUtils.hasText(keyword), Product::getProductName, keyword)
                .eq(query.getStoreId() != null, Product::getStoreId, query.getStoreId())
                .eq(productType != null, Product::getProductType, productType)
                .eq(StringUtils.hasText(query.getCategory()), Product::getCategory, query.getCategory())
                .eq(query.getStatus() != null, Product::getStatus, query.getStatus())
                .orderByDesc(Product::getCreateTime);
        return convertPage(page(new Page<>(pageNum, pageSize), wrapper));
    }

    @Override
    public Product requireOwnedProduct(Long productId, Long merchantUserId) {
        Product product = getActiveProduct(productId);
        Store store = storeMapper.selectById(product.getStoreId());
        if (store == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品所属商店不存在");
        }
        if (merchantUserId == null || !merchantUserId.equals(store.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该商品");
        }
        return product;
    }

    @Override
    public ProductVO getMerchantDetail(Long id, Long merchantUserId) {
        return toVO(requireOwnedProduct(id, merchantUserId));
    }

    @Override
    public ProductVO createMerchantProduct(ProductCreateDTO dto, Long merchantUserId) {
        requireOwnedStore(dto == null ? null : dto.getStoreId(), merchantUserId);
        return createProduct(dto);
    }

    @Override
    public ProductVO updateMerchantProduct(Long id, ProductUpdateDTO dto, Long merchantUserId) {
        Product product = requireOwnedProduct(id, merchantUserId);
        if (dto != null && requestsOnline(dto.getStatus())) {
            ensureNotPlatformRestricted(product);
        }
        if (dto != null && dto.getStoreId() != null) {
            requireOwnedStore(dto.getStoreId(), merchantUserId);
        }
        return updateProduct(id, dto);
    }

    @Override
    public Boolean deleteMerchantProduct(Long id, Long merchantUserId) {
        requireOwnedProduct(id, merchantUserId);
        return deleteProduct(id);
    }

    @Override
    public ProductVO onlineMerchantProduct(Long id, Long merchantUserId) {
        requireOwnedProduct(id, merchantUserId);
        return onlineProduct(id);
    }

    @Override
    public ProductVO offlineMerchantProduct(Long id, Long merchantUserId) {
        requireOwnedProduct(id, merchantUserId);
        return offlineProduct(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO forceOfflineProduct(Long id, String reason, Long offlineUserId) {
        Product product = getActiveProduct(id);
        if (product.getStatus() == STATUS_SOLD) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "已售出商品不能强制下架");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "强制下架原因不能为空");
        }
        if (reason.trim().length() > 500) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "强制下架原因长度不能超过500");
        }
        if (offlineUserId == null) {
            throw new BusinessException(ErrorCode.NOT_AUTH, "未获取到当前监管人员");
        }
        Product update = new Product();
        update.setId(id);
        update.setStatus(STATUS_OFFLINE);
        update.setOfflineReason(reason.trim());
        update.setOfflineUserId(offlineUserId);
        update.setOfflineTime(LocalDateTime.now());
        if (!updateById(update)) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "商品强制下架失败");
        }
        product.setStatus(STATUS_OFFLINE);
        product.setOfflineReason(update.getOfflineReason());
        product.setOfflineUserId(update.getOfflineUserId());
        product.setOfflineTime(update.getOfflineTime());
        return toVO(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO releaseOfflineRestriction(Long id) {
        Product product = getActiveProduct(id);
        int rows = baseMapper.update(null, new LambdaUpdateWrapper<Product>()
                .set(Product::getStatus, STATUS_OFFLINE)
                .set(Product::getOfflineReason, null)
                .set(Product::getOfflineUserId, null)
                .set(Product::getOfflineTime, null)
                .eq(Product::getId, id)
                .eq(Product::getDeleted, 0));
        if (rows != 1) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "解除商品平台限制失败");
        }
        product.setStatus(STATUS_OFFLINE);
        product.setOfflineReason(null);
        product.setOfflineUserId(null);
        product.setOfflineTime(null);
        return toVO(product);
    }

    private Store requireOwnedStore(Long storeId, Long merchantUserId) {
        if (storeId == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商店ID不能为空");
        }
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商店不存在");
        }
        if (merchantUserId == null || !merchantUserId.equals(store.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权使用该商店");
        }
        return store;
    }

    private Product getActiveProduct(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商品ID不能为空");
        }
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        return product;
    }

    private void checkOperatingStore(Long storeId) {
        if (storeId == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商店ID不能为空");
        }
        Integer count = productStoreLookupMapper.existsOperatingStore(storeId);
        if (count == null || count == 0) {
            Integer undeletedCount = productStoreLookupMapper.existsUndeletedStore(storeId);
            if (undeletedCount != null && undeletedCount > 0) {
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "商店未营业，不能发布商品");
            }
            throw new BusinessException(ErrorCode.NOT_FOUND, "商店不存在");
        }
    }

    private void validateCreateRequired(ProductCreateDTO dto) {
        if (dto == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商品参数不能为空");
        }
        if (!StringUtils.hasText(dto.getProductName())) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商品名称不能为空");
        }
        if (dto.getProductType() == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商品类型不能为空");
        }
        if (dto.getPrice() == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商品价格不能为空");
        }
        if (dto.getStock() == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "商品库存不能为空");
        }
    }

    private String normalizeImages(String images) {
        if (!StringUtils.hasText(images)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(images);
            // images 必须是 JSON 字符串数组，避免非法 JSON 写入 MySQL JSON 字段。
            if (!root.isArray()) {
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品图片必须是JSON字符串数组");
            }
            for (JsonNode item : root) {
                if (!item.isTextual() || !StringUtils.hasText(item.asText())) {
                    throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品图片必须是非空字符串数组");
                }
            }
            return images.trim();
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品图片格式错误");
        }
    }

    private void throwOnlineFailure(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在或已删除");
        }
        if (product.getStatus() == STATUS_ONLINE) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品已经上架");
        }
        if (product.getStatus() == STATUS_SOLD) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "已售出的商品不能重新上架");
        }
        ensureNotPlatformRestricted(product);
        if (product.getStock() == null || product.getStock() <= 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "库存为0的商品不能上架");
        }
        checkOperatingStore(product.getStoreId());
    }

    private void validateBusinessRules(Integer productType, Integer stock, BigDecimal price, Integer status, boolean wasSold) {
        if (productType == null || (productType != TYPE_PET && productType != TYPE_GOODS)) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品类型只能为1或2");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "价格不能小于0");
        }
        if (stock == null || stock < 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "库存不能小于0");
        }
        if (status == null || status < STATUS_OFFLINE || status > STATUS_SOLD) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品状态只能为0、1或2");
        }
        if (productType == TYPE_PET && stock > 1) {
            // 宠物是活体，一件商品只表示一只宠物，所以库存只能是0或1。
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "活体宠物每条商品代表一只，库存只能是0或1");
        }
        if (status == STATUS_ONLINE && stock == 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "库存为0的商品不能上架");
        }
        if (wasSold && status != STATUS_SOLD) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "已售出商品不能修改售出状态");
        }
        if (!wasSold && status == STATUS_SOLD) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "已售出状态由订单流程控制，不能手工设置");
        }
    }

    private IPage<ProductVO> convertPage(Page<Product> page) {
        Map<Long, Store> storeMap = loadStoreMap(page.getRecords());
        return page.convert(product -> toVO(product, storeMap));
    }

    private Map<Long, Store> loadStoreMap(Collection<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> storeIds = products.stream()
                .map(Product::getStoreId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (storeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Store> stores = storeMapper.selectBatchIds(storeIds);
        return stores.stream().collect(Collectors.toMap(Store::getId, Function.identity(), (a, b) -> a));
    }

    private ProductVO toVO(Product product) {
        Store store = productStoreLookupMapper.selectUndeletedStore(product.getStoreId());
        Map<Long, Store> storeMap = store == null ? Collections.emptyMap() : Map.of(store.getId(), store);
        return toVO(product, storeMap);
    }

    private ProductVO toVO(Product product, Map<Long, Store> storeMap) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setStoreId(product.getStoreId());
        vo.setProductName(product.getProductName());
        vo.setProductType(product.getProductType());
        vo.setCategory(product.getCategory());
        vo.setProductDesc(product.getProductDesc());
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        vo.setMainImage(product.getMainImage());
        vo.setImages(product.getImages());
        vo.setStatus(statusText(product.getStatus()));
        vo.setStatusCode(product.getStatus());
        vo.setVideoId(product.getVideoId());
        vo.setOfflineReason(product.getOfflineReason());
        vo.setOfflineUserId(product.getOfflineUserId());
        vo.setOfflineTime(product.getOfflineTime());
        vo.setPlatformRestricted(isPlatformRestricted(product));
        vo.setCreateTime(product.getCreateTime());
        vo.setUpdateTime(product.getUpdateTime());
        vo.setName(product.getProductName());
        vo.setType(productTypeText(product.getProductType()));
        vo.setDetail(product.getProductDesc());
        vo.setImage(product.getMainImage());
        vo.setStore(toStoreVO(storeMap.get(product.getStoreId())));
        return vo;
    }

    private ProductStoreVO toStoreVO(Store store) {
        if (store == null) {
            return null;
        }
        ProductStoreVO vo = new ProductStoreVO();
        vo.setId(store.getId());
        vo.setStoreName(store.getStoreName());
        vo.setStoreLogo(store.getStoreLogo());
        vo.setStorePhone(store.getStorePhone());
        vo.setProvince(store.getProvince());
        vo.setCity(store.getCity());
        vo.setDistrict(store.getDistrict());
        vo.setAddress(store.getAddress());
        vo.setStatus(store.getStatus());
        vo.setStatusText(storeStatusText(store.getStatus()));
        return vo;
    }

    private String storeStatusText(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "营业中";
            case 2 -> "已关闭";
            case 3 -> "审核驳回";
            default -> String.valueOf(status);
        };
    }

    private Integer parseEditableStatus(String status, Integer defaultStatus) {
        if (!StringUtils.hasText(status)) {
            return defaultStatus;
        }
        String value = status.trim();
        return switch (value) {
            case "0", "下架" -> STATUS_OFFLINE;
            case "1", "上架" -> STATUS_ONLINE;
            case "2", "已售出" -> throw new BusinessException(ErrorCode.FARAMS_ERROR, "已售出状态由订单流程控制，不能手工设置");
            default -> throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品状态只能为0、1或上架、下架");
        };
    }

    private String statusText(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case STATUS_OFFLINE -> "下架";
            case STATUS_ONLINE -> "上架";
            case STATUS_SOLD -> "已售出";
            default -> String.valueOf(status);
        };
    }

    private String productTypeText(Integer productType) {
        if (productType == null) {
            return null;
        }
        return productType == TYPE_PET ? "活体宠物" : "宠物用品/周边";
    }

    private Integer resolveProductType(ProductQueryDTO query) {
        if (query.getProductType() != null) {
            return query.getProductType();
        }
        if (!StringUtils.hasText(query.getType())) {
            return null;
        }
        return switch (query.getType().trim()) {
            case "1", "宠物", "活体宠物" -> TYPE_PET;
            case "2", "周边", "宠物周边", "宠物用品", "宠物用品/周边" -> TYPE_GOODS;
            default -> throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品类型只能为1、2、活体宠物或宠物用品/周边");
        };
    }

    private boolean requestsOnline(String status) {
        return StringUtils.hasText(status) && ("1".equals(status.trim()) || "上架".equals(status.trim()));
    }

    private boolean isPlatformRestricted(Product product) {
        return product != null && (StringUtils.hasText(product.getOfflineReason())
                || product.getOfflineUserId() != null || product.getOfflineTime() != null);
    }

    private void ensureNotPlatformRestricted(Product product) {
        if (isPlatformRestricted(product)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "商品因平台强制下架仍受限制，请联系监管人员解除限制后再上架");
        }
    }

    private String resolveKeyword(ProductQueryDTO query) {
        String keyword = StringUtils.hasText(query.getKeyword()) ? query.getKeyword() : query.getProductName();
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }



    private long resolvePageSize(ProductQueryDTO query) {
        Long size = query.getSize() != null ? query.getSize() : query.getPageSize();
        return size == null ? 10L : Math.min(size, 100L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long productId, Integer quantity) {
        if (productId == null || quantity == null || quantity <= 0) {
            return false;
        }
        int rows = baseMapper.update(null, new LambdaUpdateWrapper<Product>()
                .setSql("stock = stock - " + quantity)
                .eq(Product::getId, productId)
                .ge(Product::getStock, quantity)
                .eq(Product::getStatus, STATUS_ONLINE));
        return rows > 0;
    }
}
