package com.pat.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.common.domain.ErrorCode;
import com.pat.common.domain.Result;
import com.pat.common.exception.BusinessException;
import com.pat.user.controller.AuthController;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.dto.LoginVO;
import com.pat.user.domain.entity.User;
import com.pat.user.interceptor.AdminAuthInterceptor;
import com.pat.user.interceptor.AuditorAuthInterceptor;
import com.pat.user.interceptor.MerchantAuthInterceptor;
import com.pat.user.service.auth.AuthService;
import com.pat.user.service.auth.AuthServiceRouter;
import com.pat.user.utils.JwtUtil;
import com.pat.user.utils.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ManagementAuthTest {

    private final AuthService authService = mock(AuthService.class);
    private final AuthServiceRouter router = mock(AuthServiceRouter.class);
    private final AuthController controller = new AuthController();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "authServiceRouter", router);
        when(router.getService("password")).thenReturn(authService);
    }

    @AfterEach
    void cleanUp() {
        UserHolder.remove();
    }

    @Test
    void adminManagementLoginSucceeds() {
        assertManagementLogin("admin");
    }

    @Test
    void auditorManagementLoginSucceeds() {
        assertManagementLogin("auditor");
    }

    @Test
    void merchantManagementLoginSucceeds() {
        assertManagementLogin("merchant");
    }

    @Test
    void ordinaryUserManagementLoginIsRejected() {
        when(authService.authenticate(any())).thenReturn(user("user"));
        Result<LoginVO> result = controller.adminLogin(login());
        assertThat(result.getCode()).isEqualTo(403);
        assertThat(result.getData()).isNull();
    }

    @Test
    void disabledAccountManagementLoginIsRejected() {
        when(authService.authenticate(any())).thenThrow(new BusinessException(ErrorCode.USER_DISABLED));
        assertThatThrownBy(() -> controller.adminLogin(login()))
                .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getCode()).isEqualTo(403));
    }

    @Test
    void missingTokenOnMerchantEndpointReturns401() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean allowed = new MerchantAuthInterceptor(objectMapper)
                .preHandle(new MockHttpServletRequest(), response, new Object());
        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void userRoleOnMerchantEndpointReturns403() throws Exception {
        MockHttpServletResponse response = authorize(new MerchantAuthInterceptor(objectMapper), "user");
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void merchantRoleOnAuditorEndpointReturns403() throws Exception {
        MockHttpServletResponse response = authorize(new AuditorAuthInterceptor(objectMapper), "merchant");
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void auditorRoleOnAdminEndpointReturns403() throws Exception {
        MockHttpServletResponse response = authorize(new AdminAuthInterceptor(objectMapper), "auditor");
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void adminRoleOnAuditorEndpointSucceedsAndPopulatesContext() throws Exception {
        AuditorAuthInterceptor interceptor = new AuditorAuthInterceptor(objectMapper);
        MockHttpServletRequest request = requestWithRole("admin");
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(UserHolder.getUserId()).isEqualTo(1L);
        assertThat(UserHolder.getUsername()).isEqualTo("tester");
        assertThat(UserHolder.getRole()).isEqualTo("admin");
    }

    private void assertManagementLogin(String role) {
        when(authService.authenticate(any())).thenReturn(user(role));
        Result<LoginVO> result = controller.adminLogin(login());
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getRole()).isEqualTo(role);
        assertThat(JwtUtil.parseToken(result.getData().getToken()).get("role", String.class)).isEqualTo(role);
    }

    private MockHttpServletResponse authorize(com.pat.user.interceptor.RoleAuthInterceptor interceptor, String role) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        interceptor.preHandle(requestWithRole(role), response, new Object());
        return response;
    }

    private MockHttpServletRequest requestWithRole(String role) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + JwtUtil.generateToken(1L, "tester", role, 60_000));
        return request;
    }

    private LoginDTO login() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("tester");
        dto.setPassword("secret");
        return dto;
    }

    private User user(String role) {
        User user = new User();
        user.setId(1L);
        user.setUsername("tester");
        user.setRole(role);
        user.setStatus(1);
        return user;
    }
}
