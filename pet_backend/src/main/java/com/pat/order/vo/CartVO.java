package com.pat.order.vo;

import com.pat.order.domain.entity.Cart;
import com.pat.product.domain.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CartVO extends Cart {
    private Product productInfo;
}
