package com.pat.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pat.common.domain.BaseEntity;
import com.pat.product.domain.dto.ProductCreateDTO;
import com.pat.product.domain.dto.ProductUpdateDTO;
import org.springframework.util.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private Long storeId;

    private String productName;

    private Integer productType;

    private String category;

    private String productDesc;

    private BigDecimal price;

    private Integer stock;

    private String mainImage;

    private String images;

    private Integer status;

    private Long videoId;

    private String offlineReason;

    private Long offlineUserId;

    private LocalDateTime offlineTime;

    @TableLogic(value = "0", delval = "1")
    @TableField("deleted")
    private Integer deleted;

    /**
     * 从创建 DTO 构建新商品实体。
     * Service 调用，Controller 不直接碰 Entity。
     *
     * @param dto    创建参数
     * @param status 商品状态（0下架/1上架）
     * @param images 图片 JSON 串
     * @return 新构造的商品实体
     */
    public static Product createFrom(ProductCreateDTO dto, Integer status, String images) {
        Product p = new Product();
        p.setStoreId(dto.getStoreId());
        p.setProductName(dto.getProductName());
        p.setProductType(dto.getProductType());
        p.setCategory(dto.getCategory());
        p.setProductDesc(dto.getProductDesc());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());
        p.setMainImage(dto.getMainImage());
        p.setImages(images);
        p.setStatus(status);
        p.setDeleted(0);
        return p;
    }

    /**
     * 合并更新，保留已售出商品的不可变字段。
     * 部分更新字段为 null 时回退到旧值。
     *
     * @param old         原商品（用于回退未传字段）
     * @param dto         更新参数
     * @param storeId     店铺 ID
     * @param productType 商品类型
     * @param stock       库存
     * @param price       价格
     * @param status      状态
     * @param images      图片 JSON
     * @return 新构造的更新后实体
     */
    public static Product mergeFrom(Product old, ProductUpdateDTO dto, Long storeId,
                                          Integer productType, Integer stock, BigDecimal price,
                                          Integer status, String images) {
        Product p = new Product();
        p.setId(old.getId());
        p.setStoreId(storeId);
        p.setProductName(StringUtils.hasText(dto.getProductName()) ? dto.getProductName() : old.getProductName());
        p.setProductType(productType);
        p.setCategory(dto.getCategory() == null ? old.getCategory() : dto.getCategory());
        p.setProductDesc(dto.getProductDesc() == null ? old.getProductDesc() : dto.getProductDesc());
        p.setPrice(price);
        p.setStock(stock);
        p.setMainImage(dto.getMainImage() == null ? old.getMainImage() : dto.getMainImage());
        p.setImages(images);
        p.setStatus(status);
        p.setDeleted(null);
        p.setCreateTime(null);
        p.setVideoId(old.getVideoId());
        return p;
    }

}