package com.pat.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.controller.BaseController;
import com.pat.user.domain.entity.UserAddress;
import com.pat.user.service.IUserAddressService;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "收货地址", description = "用户地址 CRUD")
@RequestMapping("/api/user/address")
public class UserAddressController extends BaseController<UserAddress, UserAddress, UserAddress> {

    public UserAddressController(IUserAddressService service) {
        super(service);
    }

    @Override
    protected UserAddress toVO(UserAddress entity) {
        return entity;
    }

    @Override
    protected UserAddress toDO(UserAddress param) {
        return param;
    }

    @Override
    protected QueryWrapper<UserAddress> buildQueryWrapper(UserAddress param) {
        return new QueryWrapper<>();
    }
}