package com.pat.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.domain.Result;
import com.pat.common.util.UserHolder;
import com.pat.user.domain.entity.UserAddress;
import com.pat.user.service.IUserAddressService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@Tag(name = "收货地址", description = "用户地址 CRUD")
@RequestMapping("/api/user/address")
public class UserAddressController {

    private final IUserAddressService addressService;

    public UserAddressController(IUserAddressService addressService) {
        this.addressService = addressService;
    }

    @Operation(summary = "根据 ID 查询地址（校验归属）")
    @GetMapping("/{id}")
    public Result<UserAddress> getById(@PathVariable Long id) {
        UserAddress addr = addressService.getById(id);
        if (addr == null) return Result.error("地址不存在");
        if (!UserHolder.getUserId().equals(addr.getUserId())) {
            return Result.error(403, "无权访问该地址");
        }
        return Result.success(addr);
    }

    @Operation(summary = "新增地址")
    @PostMapping
    public Result<Boolean> save(@RequestBody UserAddress param) {
        param.setUserId(UserHolder.getUserId());
        return Result.success(addressService.save(param));
    }

    @Operation(summary = "修改地址（校验归属）")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UserAddress param) {
        UserAddress addr = addressService.getById(id);
        if (addr == null) return Result.error("地址不存在");
        if (!UserHolder.getUserId().equals(addr.getUserId())) {
            return Result.error(403, "无权修改该地址");
        }
        param.setId(id);
        param.setUserId(UserHolder.getUserId());
        return Result.success(addressService.updateById(param));
    }

    @Operation(summary = "删除地址（校验归属）")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        UserAddress addr = addressService.getById(id);
        if (addr == null) return Result.error("地址不存在");
        if (!UserHolder.getUserId().equals(addr.getUserId())) {
            return Result.error(403, "无权删除该地址");
        }
        return Result.success(addressService.removeById(id));
    }

    @DeleteMapping("/batch")
    public Result<Boolean> removeBatch(@RequestBody List<Long> ids) {
        return Result.success(addressService.removeByIds(ids));
    }

    @GetMapping("/search")
    public Result<IPage<UserAddress>> search(UserAddress param, Page<UserAddress> page) {
        QueryWrapper<UserAddress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", UserHolder.getUserId()).orderByDesc("create_time");
        return Result.success(addressService.page(page, wrapper));
    }

    @GetMapping("/list")
    public Result<List<UserAddress>> getList(UserAddress param) {
        QueryWrapper<UserAddress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", UserHolder.getUserId()).orderByDesc("create_time");
        return Result.success(addressService.list(wrapper));
    }

    @GetMapping("/by-ids")
    public Result<List<UserAddress>> getByIds(@RequestParam List<Long> ids) {
        return Result.success(addressService.listByIds(ids));
    }

    @PostMapping("/batch")
    public Result<Boolean> saveBatch(@RequestBody List<UserAddress> paramList) {
        for (UserAddress addr : paramList) {
            addr.setUserId(UserHolder.getUserId());
        }
        return Result.success(addressService.saveBatch(paramList));
    }

    @PutMapping("/batch")
    public Result<Boolean> updateBatch(@RequestBody List<UserAddress> paramList) {
        return Result.success(addressService.updateBatchById(paramList));
    }

    @Operation(summary = "查询默认地址（当前用户）")
    @GetMapping("/default")
    public Result<UserAddress> getDefault() {
        UserAddress addr = addressService.lambdaQuery()
                .eq(UserAddress::getUserId, UserHolder.getUserId())
                .eq(UserAddress::getDefaulted, 1)
                .last("LIMIT 1")
                .one();
        return addr != null ? Result.success(addr) : Result.error("无默认地址");
    }

    @Operation(summary = "设置默认地址")
    @PutMapping("/{id}/default")
    public Result<Boolean> setDefault(@PathVariable Long id) {
        UserAddress addr = addressService.getById(id);
        if (addr == null) return Result.error("地址不存在");
        if (!UserHolder.getUserId().equals(addr.getUserId())) {
            return Result.error(403, "无权操作该地址");
        }
        addressService.lambdaUpdate()
                .eq(UserAddress::getUserId, UserHolder.getUserId())
                .set(UserAddress::getDefaulted, 0)
                .update();
        return Result.success(addressService.lambdaUpdate()
                .eq(UserAddress::getId, id)
                .set(UserAddress::getDefaulted, 1)
                .update());
    }
}
