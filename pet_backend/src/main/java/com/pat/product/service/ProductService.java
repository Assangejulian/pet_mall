package com.pat.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.product.domain.dto.ProductCreateDTO;
import com.pat.product.domain.dto.ProductQueryDTO;
import com.pat.product.domain.dto.ProductUpdateDTO;
import com.pat.product.domain.vo.ProductPageVO;
import com.pat.product.domain.vo.ProductVO;
import com.pat.product.domain.entity.Product;

public interface ProductService extends IProductService {

    /**
     * 新增商品。
     *
     * @param dto 创建参数
     * @return 商品 VO
     */
    ProductVO createProduct(ProductCreateDTO dto);

    /**
     * 更新商品（支持部分字段）。
     *
     * @param id 商品 ID
     * @param dto 更新参数
     * @return 商品 VO
     */
    ProductVO updateProduct(Long id, ProductUpdateDTO dto);

    /**
     * 删除商品。
     *
     * @param id 商品 ID
     * @return 是否成功
     */
    Boolean deleteProduct(Long id);

    /**
     * 上架商品。
     *
     * @param id 商品 ID
     * @return 商品 VO
     */
    ProductVO onlineProduct(Long id);

    /**
     * 下架商品。
     *
     * @param id 商品 ID
     * @return 商品 VO
     */
    ProductVO offlineProduct(Long id);

    /**
     * 用户端商品分页（仅营业中店铺的在售商品）。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    IPage<ProductVO> pagePublicProducts(ProductQueryDTO query);

    /**
     * 管理端商品分页（含下架/已售出商品）。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    ProductPageVO pageAdminProducts(ProductQueryDTO query);


    /**
     * 用户端商品详情。
     *
     * @param id 商品 ID
     * @return 商品 VO
     */
    ProductVO getPublicDetail(Long id);

    /**
     * 管理端商品详情。
     *
     * @param id 商品 ID
     * @return 商品 VO
     */
    ProductVO getAdminDetail(Long id);

    /**
     * 商家端商品分页（仅商户所属店铺商品）。
     *
     * @param query 查询参数
     * @param merchantUserId 商户用户 ID
     * @return 分页结果
     */
    IPage<ProductVO> pageMerchantProducts(ProductQueryDTO query, Long merchantUserId);

    /**
     * 校验并返回商户拥有的商品。
     *
     * @param productId 商品 ID
     * @param merchantUserId 商户用户 ID
     * @return 商品实体
     */
    Product requireOwnedProduct(Long productId, Long merchantUserId);


    /**
     * 商户端新增商品（已废弃，使用 createProduct）。
     *
     * @deprecated 通过 createProduct + Controller 层鉴权替代
     */
    ProductVO createMerchantProduct(ProductCreateDTO dto, Long merchantUserId);

    /**
     * 商户端更新商品（已废弃，使用 updateProduct）。
     *
     * @deprecated 通过 updateProduct + Controller 层鉴权替代
     */
    ProductVO updateMerchantProduct(Long id, ProductUpdateDTO dto, Long merchantUserId);




    /**
     * 强制下架（审核员操作）。
     *
     * @param id 商品 ID
     * @param reason 下架原因
     * @param offlineUserId 审核员 ID
     * @return 商品 VO
     */
    ProductVO forceOfflineProduct(Long id, String reason, Long offlineUserId);

    /**
     * 解除强制下架限制（审核员操作）。
     *
     * @param id 商品 ID
     * @return 商品 VO
     */
    ProductVO releaseOfflineRestriction(Long id);

    /**
     * 扣减库存（行级锁 + 库存足量校验）。
     *
     * @param productId 商品 ID
     * @param quantity 扣减数量
     * @return true 成功，false 库存不足
     */
    boolean deductStock(Long productId, Integer quantity);

    /**
     * 恢复库存（订单取消/超时回滚时调用）。
     *
     * @param productId 商品 ID
     * @param quantity 恢复数量
     * @return true 成功
     */
    boolean restoreStock(Long productId, Integer quantity);
}