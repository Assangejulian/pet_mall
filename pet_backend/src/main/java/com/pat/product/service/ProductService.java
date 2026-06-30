package com.pat.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pat.product.domain.dto.ProductCreateDTO;
import com.pat.product.domain.dto.ProductQueryDTO;
import com.pat.product.domain.dto.ProductUpdateDTO;
import com.pat.product.domain.vo.ProductPageVO;
import com.pat.product.domain.vo.ProductVO;
import com.pat.product.domain.entity.Product;

public interface ProductService extends IProductService {

    ProductVO createProduct(ProductCreateDTO dto);

    ProductVO updateProduct(Long id, ProductUpdateDTO dto);

    Boolean deleteProduct(Long id);

    ProductVO onlineProduct(Long id);

    ProductVO offlineProduct(Long id);

    IPage<ProductVO> pagePublicProducts(ProductQueryDTO query);

    ProductPageVO pageAdminProducts(ProductQueryDTO query);

    ProductVO getPublicDetail(Long id);

    ProductVO getAdminDetail(Long id);

    IPage<ProductVO> pageMerchantProducts(ProductQueryDTO query, Long merchantUserId);

    Product requireOwnedProduct(Long productId, Long merchantUserId);

    ProductVO getMerchantDetail(Long id, Long merchantUserId);

    ProductVO createMerchantProduct(ProductCreateDTO dto, Long merchantUserId);

    ProductVO updateMerchantProduct(Long id, ProductUpdateDTO dto, Long merchantUserId);

    Boolean deleteMerchantProduct(Long id, Long merchantUserId);

    ProductVO onlineMerchantProduct(Long id, Long merchantUserId);

    ProductVO offlineMerchantProduct(Long id, Long merchantUserId);

    ProductVO forceOfflineProduct(Long id);
}
