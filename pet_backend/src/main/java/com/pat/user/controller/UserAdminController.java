package com.pat.user.controller;
import cn.hutool.core.bean.BeanUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.common.controller.BaseController;
import com.pat.common.domain.Result;
import com.pat.user.domain.dto.UserQueryParam;
import com.pat.user.domain.entity.User;
import com.pat.user.service.UserService;
import com.pat.user.domain.vo.UserVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
@Tag(name = "用户管理（后台）")
public class UserAdminController extends BaseController<User, UserQueryParam, UserVO> {

    public UserAdminController(UserService userService) {
        super(userService);
    }

    @Override
    protected UserVO toVO(User entity) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected User toDO(UserQueryParam param) {
        User user = new User();
        if (param != null) {
            user.setUsername(param.getKeyword());
            user.setStatus(param.getStatus());
        }
        return user;
    }

    @Override
    protected QueryWrapper<User> buildQueryWrapper(UserQueryParam param) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (param == null) return wrapper;
        if (param.getKeyword() != null && !param.getKeyword().isBlank())
            wrapper.and(w -> w.like("username", param.getKeyword()).or().like("phone", param.getKeyword()));
        if (param.getStatus() != null) wrapper.eq("status", param.getStatus());
        return wrapper;
    }
}
