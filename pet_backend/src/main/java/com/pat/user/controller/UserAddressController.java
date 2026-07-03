package com.pat.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.controller.BaseController;
import com.pat.common.domain.Result;
import com.pat.common.util.UserHolder;
import com.pat.user.domain.entity.UserAddress;
import com.pat.user.service.IUserAddressService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

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
        QueryWrapper<UserAddress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", UserHolder.getUserId());
        return wrapper;
    }

    @Override
    protected void preSave(UserAddress param) {
        param.setUserId(UserHolder.getUserId());
    }

    @Override
    @Operation(summary = "根据 ID 查询地址（校验归属）")
    @GetMapping("/{id}")
    public Result<UserAddress> getById(@PathVariable Long id) {
        UserAddress addr = baseService.getById(id);
        if (addr == null) return Result.error("地址不存在");
        if (!UserHolder.getUserId().equals(addr.getUserId())) {
            return Result.error(403, "无权访问该地址");
        }
        return Result.success(toVO(addr));
    }

    @Override
    @Operation(summary = "修改地址（校验归属）")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UserAddress param) {
        UserAddress addr = baseService.getById(id);
        if (addr == null) return Result.error("地址不存在");
        if (!UserHolder.getUserId().equals(addr.getUserId())) {
            return Result.error(403, "无权修改该地址");
        }
        param.setId(id);
        param.setUserId(UserHolder.getUserId());
        return Result.success(baseService.updateById(param));
    }

    @Override
    @Operation(summary = "删除地址（校验归属）")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        UserAddress addr = baseService.getById(id);
        if (addr == null) return Result.error("地址不存在");
        if (!UserHolder.getUserId().equals(addr.getUserId())) {
            return Result.error(403, "无权删除该地址");
        }
        return Result.success(baseService.removeById(id));
    }

    @Operation(summary = "查询默认地址（当前用户）")
    @GetMapping("/default")
    public Result<UserAddress> getDefault() {
        UserAddress addr = baseService.lambdaQuery()
                .eq(UserAddress::getUserId, UserHolder.getUserId())
                .eq(UserAddress::getDefaulted, 1)
                .last("LIMIT 1")
                .one();
        return addr != null ? Result.success(addr) : Result.error("无默认地址");
    }

    @Operation(summary = "设置默认地址")
    @PutMapping("/{id}/default")
    public Result<Boolean> setDefault(@PathVariable Long id) {
        UserAddress addr = baseService.getById(id);
        if (addr == null) return Result.error("地址不存在");
        if (!UserHolder.getUserId().equals(addr.getUserId())) {
            return Result.error(403, "无权操作该地址");
        }
        baseService.lambdaUpdate()
                .eq(UserAddress::getUserId, UserHolder.getUserId())
                .set(UserAddress::getDefaulted, 0)
                .update();
        return Result.success(baseService.lambdaUpdate()
                .eq(UserAddress::getId, id)
                .set(UserAddress::getDefaulted, 1)
                .update());
    }
}
