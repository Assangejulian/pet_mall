package com.pat.user.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.Result;
import com.pat.user.domain.dto.UserQueryParam;
import com.pat.user.domain.entity.User;
import com.pat.user.service.UserService;
import com.pat.user.domain.vo.UserVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user")
@Tag(name = "用户管理（后台）")
public class UserAdminController {

    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户")
    public Result<UserVO> getById(@PathVariable Long id) {
        User entity = userService.getById(id);
        return entity == null ? Result.error("数据不存在") : Result.success(toVO(entity));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Boolean> save(@RequestBody @Valid UserQueryParam param) {
        User entity = toDO(param);
        return Result.success(userService.save(entity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid UserQueryParam param) {
        User entity = toDO(param);
        entity.setId(id);
        return Result.success(userService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户")
    public Result<Boolean> removeBatch(@RequestBody List<Long> ids) {
        return Result.success(userService.removeByIds(ids));
    }

    @GetMapping("/search")
    @Operation(summary = "分页搜索用户")
    public Result<IPage<UserVO>> search(UserQueryParam param, Page<User> page) {
        Page<User> result = userService.page(page, buildQueryWrapper(param));
        return Result.success(result.convert(this::toVO));
    }

    @GetMapping("/list")
    @Operation(summary = "获取用户列表")
    public Result<List<UserVO>> getList(UserQueryParam param) {
        List<User> list = userService.list(buildQueryWrapper(param));
        return Result.success(list.stream().map(this::toVO).toList());
    }

    @GetMapping("/by-ids")
    @Operation(summary = "根据ID列表批量获取用户")
    public Result<List<UserVO>> getByIds(@RequestParam List<Long> ids) {
        List<User> list = userService.listByIds(ids);
        return Result.success(list.stream().map(this::toVO).toList());
    }

    @PostMapping("/batch")
    @Operation(summary = "批量新增用户")
    public Result<Boolean> saveBatch(@RequestBody @Valid List<UserQueryParam> paramList) {
        List<User> entities = paramList.stream().map(this::toDO).toList();
        return Result.success(userService.saveBatch(entities));
    }

    @PutMapping("/batch")
    @Operation(summary = "批量更新用户")
    public Result<Boolean> updateBatch(@RequestBody @Valid List<UserQueryParam> paramList) {
        List<User> entities = paramList.stream().map(this::toDO).toList();
        return Result.success(userService.updateBatchById(entities));
    }

    private UserVO toVO(User entity) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }

    private User toDO(UserQueryParam param) {
        User user = new User();
        if (param != null) {
            user.setUsername(param.getKeyword());
            user.setStatus(param.getStatus());
            user.setMemberLevel(param.getMemberLevel());
        }
        return user;
    }

    private QueryWrapper<User> buildQueryWrapper(UserQueryParam param) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (param == null) return wrapper;
        if (param.getKeyword() != null && !param.getKeyword().isBlank())
            wrapper.and(w -> w.like("username", param.getKeyword()).or().like("phone", param.getKeyword()));
        if (param.getStatus() != null) wrapper.eq("status", param.getStatus());
        return wrapper;
    }
}
