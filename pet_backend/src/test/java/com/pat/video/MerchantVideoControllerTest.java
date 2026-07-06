package com.pat.video;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.Result;
import com.pat.common.exception.BusinessException;
import com.pat.common.util.UserHolder;
import com.pat.video.controller.MerchantVideoController;
import com.pat.video.domain.entity.Video;
import com.pat.video.service.IVideoService;
import com.pat.product.service.ProductService;
import com.pat.product.domain.entity.Product;
import com.pat.store.service.IStoreService;
import com.pat.store.domain.entity.Store;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class MerchantVideoControllerTest {

    private final IVideoService videoService = mock(IVideoService.class);
    private final ProductService productService = mock(ProductService.class);
    private final IStoreService storeService = mock(IStoreService.class);
    private final MerchantVideoController controller = new MerchantVideoController(
            videoService, productService, storeService);

    @AfterEach
    void cleanup() {
        UserHolder.remove();
    }

    @Test
    void merchantVideoSearchIsScopedToCurrentMerchantProducts() {
        UserHolder.save("userId", 11L);
        Video video = video(1L, 501L);
        video.setTitle("merchant-video");
        Page<Video> source = new Page<>(1, 10, 1);
        source.setRecords(List.of(video));
        when(videoService.page(any(Page.class), any(Wrapper.class))).thenReturn(source);
        Result<IPage<Video>> result = controller.search(1, 10, "merchant", 1);

        assertThat(result.getData().getRecords()).hasSize(1);
        ArgumentCaptor<QueryWrapper<Video>> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(videoService).page(any(Page.class), wrapperCaptor.capture());
        String sql = wrapperCaptor.getValue().getSqlSegment();
        assertThat(sql).contains("product_id IN (SELECT p.id FROM product", "s.user_id = 11", "title", "status");
    }

    @Test
    void merchantCannotReadVideoOutsideOwnProducts() {
        UserHolder.save("userId", 11L);
        when(videoService.getOne(any(Wrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> controller.detail(2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权访问");
    }

    @Test
    void merchantCanReadOwnVideoDetail() {
        UserHolder.save("userId", 11L);
        Video video = video(3L, 503L);
        video.setTitle("owned");
        when(videoService.getOne(any(Wrapper.class))).thenReturn(video);

        Result<Video> result = controller.detail(3L);

        assertThat(result.getData().getId()).isEqualTo(3L);
        assertThat(result.getData().getProductId()).isEqualTo(503L);
    }

    @Test
    void createRejectsProductOwnedByAnotherMerchant() {
        UserHolder.save("userId", 11L);
        when(productService.getById(501L)).thenReturn(product(501L, 21L));
        when(storeService.getById(21L)).thenReturn(store(21L, 22L));
        Video request = video(null, 501L);

        assertThatThrownBy(() -> controller.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权关联");
        verify(videoService, never()).save(any(Video.class));
    }

    @Test
    void createForcesCurrentOwnerAndPendingStatus() {
        UserHolder.save("userId", 11L);
        when(productService.getById(501L)).thenReturn(product(501L, 20L));
        when(storeService.getById(20L)).thenReturn(store(20L, 11L));
        Video request = video(99L, 501L);
        request.setUserId(22L);
        request.setStatus(1);
        request.setPlayCount(999);

        controller.create(request);

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoService).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(11L);
        assertThat(captor.getValue().getStatus()).isZero();
        assertThat(captor.getValue().getPlayCount()).isZero();
        assertThat(captor.getValue().getId()).isNull();
    }

    @Test
    void updateCannotSwitchToAnotherMerchantsProduct() {
        UserHolder.save("userId", 11L);
        when(videoService.getOne(any(Wrapper.class))).thenReturn(video(3L, 501L));
        when(productService.getById(502L)).thenReturn(product(502L, 22L));
        when(storeService.getById(22L)).thenReturn(store(22L, 22L));
        Video request = video(null, 502L);

        assertThatThrownBy(() -> controller.update(3L, request))
                .isInstanceOf(BusinessException.class);
        verify(videoService, never()).updateById(any(Video.class));
    }

    @Test
    void deleteRejectsVideoOutsideMerchantProducts() {
        UserHolder.save("userId", 11L);
        when(videoService.getOne(any(Wrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> controller.delete(4L)).isInstanceOf(BusinessException.class);
        verify(videoService, never()).removeById(4L);
    }

    @Test
    void onlineRejectsVideoOutsideMerchantProducts() {
        UserHolder.save("userId", 11L);
        when(videoService.getOne(any(Wrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> controller.online(4L)).isInstanceOf(BusinessException.class);
        verify(videoService, never()).updateById(any(Video.class));
    }

    @Test
    void offlineRejectsVideoOutsideMerchantProducts() {
        UserHolder.save("userId", 11L);
        when(videoService.getOne(any(Wrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> controller.offline(4L)).isInstanceOf(BusinessException.class);
        verify(videoService, never()).updateById(any(Video.class));
    }

    private static Video video(Long id, Long productId) {
        Video video = new Video();
        video.setId(id);
        video.setProductId(productId);
        video.setStatus(1);
        return video;
    }

    private static Product product(Long id, Long storeId) {
        Product product = new Product();
        product.setId(id);
        product.setStoreId(storeId);
        return product;
    }

    private static Store store(Long id, Long ownerId) {
        Store store = new Store();
        store.setId(id);
        store.setUserId(ownerId);
        return store;
    }
}
