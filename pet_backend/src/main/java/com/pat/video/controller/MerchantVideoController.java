package com.pat.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.ErrorCode;
import com.pat.common.domain.Result;
import com.pat.common.exception.BusinessException;
import com.pat.common.util.UserHolder;
import com.pat.product.domain.entity.Product;
import com.pat.product.service.ProductService;
import com.pat.store.domain.entity.Store;
import com.pat.store.service.IStoreService;
import com.pat.video.domain.entity.Video;
import com.pat.video.service.IVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "视频管理(商家端)", description = "商家端视频 CRUD、上下架")
@RestController
@RequestMapping("/api/merchant/video")
public class MerchantVideoController {

    private final IVideoService videoService;
    private final ProductService productService;
    private final IStoreService storeService;

    public MerchantVideoController(IVideoService videoService,
                                   ProductService productService,
                                   IStoreService storeService) {
        this.videoService = videoService;
        this.productService = productService;
        this.storeService = storeService;
    }

    @Operation(summary = "商家端视频分页查询")
    @GetMapping("/search")
    public Result<IPage<Video>> search(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        QueryWrapper<Video> wrapper = ownedVideoWrapper(requireMerchantUserId());
        wrapper.orderByDesc("create_time");
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("title", keyword.trim());
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        return Result.success(videoService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "视频详情(商家端)")
    @GetMapping("/{id}")
    public Result<Video> detail(@PathVariable Long id) {
        return Result.success(requireOwnedVideo(id));
    }

    @Operation(summary = "新增视频")
    @PostMapping
    public Result<Video> create(@RequestBody Video request) {
        Long merchantUserId = requireMerchantUserId();
        requireOwnedProduct(request.getProductId(), merchantUserId);
        Video video = new Video();
        applyEditableFields(video, request);
        video.setUserId(merchantUserId);
        video.setStatus(0);
        video.setPlayCount(0);
        video.setLikes(0);
        video.setCommentCount(0);
        videoService.save(video);
        return Result.success(video);
    }

    @Operation(summary = "修改视频")
    @PutMapping("/{id}")
    public Result<Video> update(@PathVariable Long id, @RequestBody Video request) {
        Long merchantUserId = requireMerchantUserId();
        Video video = requireOwnedVideo(id);
        Long targetProductId = request.getProductId() == null ? video.getProductId() : request.getProductId();
        requireOwnedProduct(targetProductId, merchantUserId);
        request.setProductId(targetProductId);
        applyEditableFields(video, request);
        videoService.updateById(video);
        return Result.success(videoService.getById(id));
    }

    @Operation(summary = "删除视频")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        requireOwnedVideo(id);
        return Result.success(videoService.removeById(id));
    }

    @Operation(summary = "视频上架")
    @PutMapping("/{id}/online")
    public Result<Video> online(@PathVariable Long id) {
        return changeStatus(id, 1);
    }

    @Operation(summary = "视频下架")
    @PutMapping("/{id}/offline")
    public Result<Video> offline(@PathVariable Long id) {
        return changeStatus(id, 0);
    }

    private Result<Video> changeStatus(Long id, int status) {
        Video video = requireOwnedVideo(id);
        video.setStatus(status);
        videoService.updateById(video);
        return Result.success(video);
    }

    private Video requireOwnedVideo(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "视频ID不能为空");
        }
        Video video = videoService.getOne(ownedVideoWrapper(requireMerchantUserId()).eq("id", id));
        if (video == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "视频不存在或无权访问");
        }
        return video;
    }

    private Product requireOwnedProduct(Long productId, Long merchantUserId) {
        if (productId == null) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, "关联商品不能为空");
        }
        Product product = productService.getById(productId);
        if (product == null || product.getStoreId() == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "关联商品不存在");
        }
        Store store = storeService.getById(product.getStoreId());
        if (store == null || !merchantUserId.equals(store.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权关联该商品");
        }
        return product;
    }

    private QueryWrapper<Video> ownedVideoWrapper(Long merchantUserId) {
        return new QueryWrapper<Video>().inSql("product_id",
                "SELECT p.id FROM product p INNER JOIN store s ON s.id = p.store_id "
                        + "WHERE p.deleted = 0 AND s.deleted = 0 AND s.user_id = " + merchantUserId);
    }

    private Long requireMerchantUserId() {
        Long merchantUserId = UserHolder.getUserId();
        if (merchantUserId == null) {
            throw new BusinessException(ErrorCode.NOT_AUTH, "商家未登录");
        }
        return merchantUserId;
    }

    private void applyEditableFields(Video target, Video source) {
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setUrl(source.getUrl());
        target.setCover(source.getCover());
        target.setProductId(source.getProductId());
        target.setDuration(source.getDuration());
    }
}
