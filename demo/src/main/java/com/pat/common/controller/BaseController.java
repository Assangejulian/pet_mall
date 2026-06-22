package com.pat.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.common.domain.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通用 CRUD 控制器基类。
 *
 * <p>子类只需实现 3 个抽象方法，即可获得 10 个 REST 接口（6核心 + 4扩展）：</p>
 * <pre>
 *  GET   /{id}        查单个
 *  POST  /            新增
 *  PUT   /{id}        更新
 *  DELETE /{id}       删除
 *  DELETE /batch      批量删
 *  GET   /search      分页条件查
 *  GET   /list        不分页查
 *  GET   /by-ids      按ID批量查
 *  POST  /batch       批量增
 *  PUT   /batch       批量改
 * </pre>
 *
 * @param <S> Service，需继承 IService&lt;E&gt;，如 ProductService extends IService&lt;Product&gt;
 * @param <E> Entity 实体类，对应数据库表
 * @param <V> VO 视图对象，前端交互用，查询返回 VO，保存/更新接收 VO
 * @param <Q> Query 查询条件对象，前端传参 &amp; buildQueryWrapper 构造条件用
 */
@SuppressWarnings({"unchecked", "SpringJavaInjectionPointsAutowiringInspection"})
public abstract class BaseController<S extends IService<E>, E, V, Q> {

    @Autowired
    protected S baseService;

    // ==================== 子类必须实现的抽象方法 ====================

    /** Entity 转 VO（查出来给前端） */
    protected abstract V toVO(E entity);

    /** VO 转 Entity（前端传进来存库），含更新时的 id 赋值 */
    protected abstract E toEntity(V vo);

    /** 构造 MyBatis-Plus 查询条件（分页 / 列表共用） */
    protected abstract QueryWrapper<E> buildQueryWrapper(Q query);

    // ==================== 可选的钩子方法 ====================

    protected void preSave(V vo) {}
    protected void postSave(E entity, boolean ok) {}
    protected void preUpdate(V vo) {}
    protected void postUpdate(E entity, boolean ok) {}

    // ==================== 核心 CRUD（6个） ====================

    @GetMapping("/{id}")
    public Result<V> getById(@PathVariable Long id) {
        E entity = baseService.getById(id);
        return entity == null ? Result.error("数据不存在") : Result.success(toVO(entity));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody @Valid V vo) {
        preSave(vo);
        E entity = toEntity(vo);
        boolean ok = baseService.save(entity);
        postSave(entity, ok);
        return Result.success(ok);
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid V vo) {
        preUpdate(vo);
        E entity = toEntity(vo);
        boolean ok = baseService.updateById(entity);
        postUpdate(entity, ok);
        return Result.success(ok);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(baseService.removeById(id));
    }

    @DeleteMapping("/batch")
    public Result<Boolean> removeBatch(@RequestBody List<Long> ids) {
        return Result.success(baseService.removeByIds(ids));
    }

    @GetMapping("/search")
    public Result<Page<V>> search(Q query, Page<E> page) {
        Page<E> result = baseService.page(page, buildQueryWrapper(query));
        return Result.success((Page<V>) result.convert(this::toVO));
    }

    // ==================== 扩展 CRUD（4个） ====================

    @GetMapping("/list")
    public Result<List<V>> getList(Q query) {
        List<E> list = baseService.list(buildQueryWrapper(query));
        return Result.success(list.stream().map(this::toVO).collect(Collectors.toList()));
    }

    @GetMapping("/by-ids")
    public Result<List<V>> getByIds(@RequestParam List<Long> ids) {
        List<E> list = baseService.listByIds(ids);
        return Result.success(list.stream().map(this::toVO).collect(Collectors.toList()));
    }

    @PostMapping("/batch")
    public Result<Boolean> saveBatch(@RequestBody @Valid List<V> voList) {
        List<E> entities = voList.stream().map(this::toEntity).collect(Collectors.toList());
        return Result.success(baseService.saveBatch(entities));
    }

    @PutMapping("/batch")
    public Result<Boolean> updateBatch(@RequestBody @Valid List<V> voList) {
        List<E> entities = voList.stream().map(this::toEntity).collect(Collectors.toList());
        return Result.success(baseService.updateBatchById(entities));
    }
}