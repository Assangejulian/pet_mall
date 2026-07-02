package com.pat.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.common.domain.Result;
import com.pat.common.interceptor.AdminAuthInterceptor;
import com.pat.common.interceptor.AuditorAuthInterceptor;
import com.pat.common.interceptor.MerchantAuthInterceptor;
import com.pat.common.interceptor.UserAuthInterceptor;
import com.pat.common.util.JwtUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = MvcConfigAuthPathTest.TestConfig.class)
class MvcConfigAuthPathTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void publicStoreEndpointsDoNotRequireToken() throws Exception {
        assertOkWithoutToken("/api/store/search");
        assertOkWithoutToken("/api/store/list");
        assertOkWithoutToken("/api/store/nearby?longitude=118.08&latitude=24.48&radiusKm=10");
        assertOkWithoutToken("/api/store/10");
        assertOkWithoutToken("/api/store/10/products");
    }

    @Test
    void publicProductAndCategoryEndpointsDoNotRequireToken() throws Exception {
        assertOkWithoutToken("/api/product/list");
        assertOkWithoutToken("/api/product/10");
        assertOkWithoutToken("/api/category/list");
    }

    @Test
    void publicVideoReadEndpointsDoNotRequireToken() throws Exception {
        assertOkWithoutToken("/api/video/feed");
        assertOkWithoutToken("/api/video/10");
        assertOkWithoutToken("/api/video/play/10");
        assertOkWithoutToken("/api/video/10/comments");
    }

    @Test
    void privateUserEndpointsRequireToken() throws Exception {
        assertUnauthorizedWithoutToken("/api/order/search");
        assertUnauthorizedWithoutToken("/api/cart/list");
        assertUnauthorizedWithoutToken("/api/user/address/list");
        assertUnauthorizedWithoutToken("/api/ai/record/session/test-session");
        mockMvc.perform(post("/api/video/10/like")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/video/10/comment")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void managementRoleInterceptorsStillEnforceTheirScopes() throws Exception {
        assertOkWithRole("/api/merchant/store/search", "merchant");
        assertForbiddenWithRole("/api/auditor/store/search", "merchant");
        assertForbiddenWithRole("/api/admin/store/list", "merchant");

        assertOkWithRole("/api/auditor/store/search", "auditor");
        assertForbiddenWithRole("/api/admin/store/list", "auditor");

        assertOkWithRole("/api/admin/store/list", "admin");
        assertOkWithRole("/api/auditor/store/search", "admin");
    }

    @Test
    void storeAndProductWorkflowWritesRequireCorrectManagementRole() throws Exception {
        assertPutUnauthorized("/api/merchant/store/1");
        assertPutForbidden("/api/merchant/store/1", "auditor");
        assertPutOk("/api/merchant/store/1", "merchant");

        assertPutUnauthorized("/api/merchant/product/1/online");
        assertPutForbidden("/api/merchant/product/1/online", "auditor");
        assertPutOk("/api/merchant/product/1/online", "merchant");

        assertPutUnauthorized("/api/auditor/store/1/reject");
        assertPutForbidden("/api/auditor/store/1/reject", "merchant");
        assertPutOk("/api/auditor/store/1/reject", "auditor");

        assertPutUnauthorized("/api/auditor/product/1/release-offline");
        assertPutForbidden("/api/auditor/product/1/release-offline", "merchant");
        assertPutOk("/api/auditor/product/1/release-offline", "auditor");
        assertPutOk("/api/auditor/product/1/release-offline", "admin");
    }

    private void assertOkWithoutToken(String path) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isOk());
    }

    private void assertUnauthorizedWithoutToken(String path) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
    }

    private void assertOkWithRole(String path, String role) throws Exception {
        mockMvc.perform(get(path).header("Authorization", bearer(role))).andExpect(status().isOk());
    }

    private void assertForbiddenWithRole(String path, String role) throws Exception {
        mockMvc.perform(get(path).header("Authorization", bearer(role))).andExpect(status().isForbidden());
    }

    private String bearer(String role) {
        return "Bearer " + JwtUtil.generateToken(1L, "tester", role, 60_000);
    }

    private void assertPutUnauthorized(String path) throws Exception {
        mockMvc.perform(put(path)).andExpect(status().isUnauthorized());
    }

    private void assertPutForbidden(String path, String role) throws Exception {
        mockMvc.perform(put(path).header("Authorization", bearer(role))).andExpect(status().isForbidden());
    }

    private void assertPutOk(String path, String role) throws Exception {
        mockMvc.perform(put(path).header("Authorization", bearer(role))).andExpect(status().isOk());
    }

    @RestController
    static class TestEndpoints {

        @GetMapping({
                "/api/store/search",
                "/api/store/list",
                "/api/store/nearby",
                "/api/store/{id}",
                "/api/store/{id}/products",
                "/api/product/list",
                "/api/product/{id}",
                "/api/category/list",
                "/api/video/feed",
                "/api/video/{id}",
                "/api/video/play/{id}",
                "/api/video/{id}/comments",
                "/api/order/search",
                "/api/cart/list",
                "/api/user/address/list",
                "/api/ai/record/session/{sessionId}",
                "/api/merchant/store/search",
                "/api/auditor/store/search",
                "/api/admin/store/list"
        })
        Result<String> ok() {
            return Result.success("ok");
        }

        @PostMapping("/api/video/{id}/like")
        Result<String> like(@PathVariable Long id) {
            return Result.success("liked-" + id);
        }

        @PostMapping("/api/video/{id}/comment")
        Result<String> comment(@PathVariable Long id) {
            return Result.success("commented-" + id);
        }

        @PutMapping({
                "/api/merchant/store/{id}",
                "/api/merchant/product/{id}/online",
                "/api/auditor/store/{id}/reject",
                "/api/auditor/product/{id}/release-offline"
        })
        Result<String> managementWrite(@PathVariable Long id) {
            return Result.success("updated-" + id);
        }
    }

    @Configuration
    @EnableWebMvc
    @Import({
            MvcConfig.class,
            UserAuthInterceptor.class,
            AdminAuthInterceptor.class,
            MerchantAuthInterceptor.class,
            AuditorAuthInterceptor.class
    })
    static class TestConfig {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        TestEndpoints testEndpoints() {
            return new TestEndpoints();
        }
    }
}
