package com.pat.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pat.product.dto.ProductCreateDTO;
import com.pat.product.dto.ProductQueryDTO;
import com.pat.product.dto.ProductUpdateDTO;
import com.pat.product.vo.ProductPageVO;
import com.pat.product.vo.ProductVO;

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
}
