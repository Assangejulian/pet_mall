package com.pat.common.controller;

import com.pat.common.domain.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;


/**
 * 通用控制器基类，封装了标准的增删改查及分页查询逻辑
 * @param <S> Service层接口，需继承IService<T>
 * @param <T> 实体类类型（用于 CRUD 操作）
 * @param <V> VO类型（用于查询返回，可与 T 相同）
 * @param <Q> 查询条件对象类型
 */
@RestController
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public abstract class BaseController<S extends IService<T>, T, V, Q> {

    protected S baseService;

    @Autowired
    public void setBaseService(S baseService) {
        this.baseService = baseService;
    }

    /**
     * 根据ID查询单个实体
     */
    @GetMapping("/{id}")
    public Result<V> getById(@PathVariable @NotNull Long id) {
        T entity = baseService.getById(id);
        if (entity == null) {
            return Result.error("数据不存在");
        }
        V vo = convertToVO(entity);
        return Result.success(vo);
    }

    /**
     * 实体转VO
     */
    @SuppressWarnings("unchecked")
    protected V convertToVO(T entity) {
        try {
            Class<V> voClass = (Class<V>) ((java.lang.reflect.ParameterizedType) getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[2];
            V vo = voClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        } catch (Exception e) {
            throw new RuntimeException("VO转换失败", e);
        }
    }

    /**
     * 新增实体
     */
    @PostMapping
    public Result<Boolean> save(@RequestBody @Validated T entity) {
        boolean success = baseService.save(entity);
        if (!success) {
            return Result.error("新增失败");
        }
        return Result.success(success);
    }

    /**
     * 根据ID更新实体
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable @NotNull Long id, @RequestBody @Validated T entity) {
        // 使用 Spring ReflectionUtils 安全查找字段（自动向上查找父类）
        Field idField = ReflectionUtils.findField(entity.getClass(), "id");
        if (idField != null) {
            ReflectionUtils.makeAccessible(idField);
            ReflectionUtils.setField(idField, entity, id);
        }
        boolean success = baseService.updateById(entity);
        return Result.success(success);
    }

    /**
     * 根据ID删除实体
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable @NotNull Long id) {
        boolean success = baseService.removeById(id);
        if (!success) {
            return Result.error("删除失败");
        }
        return Result.success(null);
    }

    /**
     * 批量删除实体
     */
    @DeleteMapping("/batch")
    public Result<Boolean> removeBatch(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("ID列表不能为空");
        }
        boolean success = baseService.removeByIds(ids);
        return Result.success(success);
    }

    /**
     * 分页及条件查询
     * 注意：此方法需要子类实现，因为每个实体的查询逻辑不同
     */
    @GetMapping("/search")
    public abstract Result<Page<V>> search(Page<V> page, Q query);




}
