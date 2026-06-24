package com.pat.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.controller.BaseController;
import com.pat.common.domain.ErrorCode;
import com.pat.common.domain.Result;
import com.pat.product.dto.ProductCreateDTO;
import com.pat.product.dto.ProductParamDTO;
import com.pat.product.dto.ProductQueryDTO;
import com.pat.product.dto.ProductUpdateDTO;
import com.pat.product.entity.Product;
import com.pat.product.service.ProductService;
import com.pat.product.vo.ProductPageVO;
import com.pat.product.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/product")
@Tag(name = "商品管理接口", description = "管理端商品新增、修改、上下架、删除和分页")
public class ProductAdminController extends BaseController<Product, ProductParamDTO, ProductVO> {

    private final ProductService productService;

    public ProductAdminController(ProductService productService) {
        super(productService);
        this.productService = productService;
    }

    @Override
    protected ProductVO toVO(Product entity) {
        return productService.getAdminDetail(entity.getId());
    }

    @Override
    protected Product toDO(ProductParamDTO param) {
        Product product = new Product();
        product.setId(param.getId());
        product.setStoreId(param.getStoreId());
        product.setProductName(param.getProductName());
        product.setProductType(param.getProductType());
        product.setCategory(param.getCategory());
        product.setProductDesc(param.getProductDesc());
        product.setPrice(param.getPrice());
        product.setStock(param.getStock());
        product.setMainImage(param.getMainImage());
        product.setImages(param.getImages());
        product.setStatus(resolveQueryStatus(param));
        return product;
    }

    @Override
    protected QueryWrapper<Product> buildQueryWrapper(ProductParamDTO param) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (param == null) {
            return wrapper.orderByDesc("create_time");
        }
        String keyword = hasText(param.getKeyword()) ? param.getKeyword() : param.getProductName();
        Integer productType = resolveProductType(param);
        Integer status = resolveQueryStatus(param);
        wrapper.like(hasText(keyword), "product_name", keyword)
                .eq(param.getStoreId() != null, "store_id", param.getStoreId())
                .eq(productType != null, "product_type", productType)
                .eq(hasText(param.getCategory()), "category", param.getCategory())
                .eq(status != null, "status", status)
                .orderByDesc("create_time");
        return wrapper;
    }

    @Override
    protected boolean doSave(Product entity, ProductParamDTO param) {
        productService.createProduct(toCreateDTO(param));
        return true;
    }

    @Override
    protected boolean doUpdate(Long id, Product entity, ProductParamDTO param) {
        productService.updateProduct(id, toUpdateDTO(param));
        return true;
    }

    @Override
    protected boolean doRemove(Long id) {
        return productService.deleteProduct(id);
    }

    @Operation(summary = "管理端商品分页查询")
    @GetMapping("/page")
    public Result<ProductPageVO> page(@Valid ProductQueryDTO query) {
        return Result.success(productService.pageAdminProducts(query));
    }

    @Override
    @GetMapping("/search")
    public Result<IPage<ProductVO>> search(ProductParamDTO param, Page<Product> page) {
        ProductPageVO result = productService.pageAdminProducts(toQueryDTO(param, page));
        Page<ProductVO> voPage = new Page<>(result.getPage(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords());
        return Result.success(voPage);
    }

    @Override
    @GetMapping("/list")
    public Result<List<ProductVO>> getList(ProductParamDTO param) {
        return Result.error(ErrorCode.FARAMS_ERROR, "商品列表请使用分页接口 /search 或 /page");
    }

    @Override
    @PostMapping("/batch")
    public Result<Boolean> saveBatch(@RequestBody @Valid List<ProductParamDTO> paramList) {
        return Result.error(ErrorCode.FARAMS_ERROR, "商品不支持批量新增");
    }

    @Override
    @PutMapping("/batch")
    public Result<Boolean> updateBatch(@RequestBody @Valid List<ProductParamDTO> paramList) {
        return Result.error(ErrorCode.FARAMS_ERROR, "商品不支持批量修改");
    }

    @Override
    @DeleteMapping("/batch")
    public Result<Boolean> removeBatch(@RequestBody List<Long> ids) {
        return Result.error(ErrorCode.FARAMS_ERROR, "商品不支持批量删除");
    }

    @Operation(summary = "商品上架")
    @PutMapping("/{id}/online")
    public Result<ProductVO> online(@PathVariable Long id) {
        return Result.success(productService.onlineProduct(id));
    }

    @Operation(summary = "商品下架")
    @PutMapping("/{id}/offline")
    public Result<ProductVO> offline(@PathVariable Long id) {
        return Result.success(productService.offlineProduct(id));
    }

    private ProductCreateDTO toCreateDTO(ProductParamDTO param) {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setStoreId(param.getStoreId());
        dto.setProductName(param.getProductName());
        dto.setProductType(param.getProductType());
        dto.setCategory(param.getCategory());
        dto.setProductDesc(param.getProductDesc());
        dto.setPrice(param.getPrice());
        dto.setStock(param.getStock());
        dto.setMainImage(param.getMainImage());
        dto.setImages(param.getImages());
        dto.setStatus(param.getStatus());
        return dto;
    }

    private ProductUpdateDTO toUpdateDTO(ProductParamDTO param) {
        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setStoreId(param.getStoreId());
        dto.setProductName(param.getProductName());
        dto.setProductType(param.getProductType());
        dto.setCategory(param.getCategory());
        dto.setProductDesc(param.getProductDesc());
        dto.setPrice(param.getPrice());
        dto.setStock(param.getStock());
        dto.setMainImage(param.getMainImage());
        dto.setImages(param.getImages());
        dto.setStatus(param.getStatus());
        return dto;
    }

    private ProductQueryDTO toQueryDTO(ProductParamDTO param, Page<Product> page) {
        ProductQueryDTO dto = new ProductQueryDTO();
        dto.setStoreId(param.getStoreId());
        dto.setProductName(param.getProductName());
        dto.setKeyword(param.getKeyword());
        dto.setProductType(param.getProductType());
        dto.setType(param.getType());
        dto.setCategory(param.getCategory());
        dto.setStatus(resolveQueryStatus(param));
        dto.setPage(page.getCurrent());
        dto.setSize(page.getSize());
        return dto;
    }

    private Integer resolveProductType(ProductParamDTO param) {
        if (param.getProductType() != null) {
            return param.getProductType();
        }
        if (!hasText(param.getType())) {
            return null;
        }
        return switch (param.getType().trim()) {
            case "1", "宠物" -> 1;
            case "2", "周边", "宠物周边" -> 2;
            default -> null;
        };
    }

    private Integer resolveQueryStatus(ProductParamDTO param) {
        if (param.getStatusCode() != null) {
            return param.getStatusCode();
        }
        if (!hasText(param.getStatus())) {
            return null;
        }
        return switch (param.getStatus().trim()) {
            case "0", "下架" -> 0;
            case "1", "上架" -> 1;
            case "2", "已售出" -> 2;
            default -> null;
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
