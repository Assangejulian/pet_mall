package com.pat.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.common.domain.BaseEntity;
import com.pat.common.domain.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通用 CRUD 控制器基类。
 *
 * <p>DO 只对应数据库表，Param 合并新增、更新和查询参数，VO 只用于接口返回。</p>
 *
 * @param <E>  DO / Entity，必须继承 BaseEntity
 * @param <P>  Param，接口入参，合并 DTO 和 Query
 * @param <VO> VO，接口返回对象
 */
public abstract class BaseController<E extends BaseEntity, P, VO> {

    protected final IService<E> baseService;

    protected BaseController(IService<E> baseService) {
        this.baseService = baseService;
    }

    /** DO 转 VO，查询结果返回给前端。 */
    protected abstract VO toVO(E entity);

    /** Param 转 DO，用于新增和更新入库。 */
    protected abstract E toDO(P param);

    /** 根据 Param 构造 MyBatis-Plus 查询条件。 */
    protected abstract QueryWrapper<E> buildQueryWrapper(P param);

    protected void preSave(P param) {}
    protected void postSave(E entity, boolean ok) {}
    protected void preUpdate(P param) {}
    protected void postUpdate(E entity, boolean ok) {}

    @GetMapping("/{id}")
    public Result<VO> getById(@PathVariable Long id) {
        E entity = baseService.getById(id);
        return entity == null ? Result.error("数据不存在") : Result.success(toVO(entity));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody @Valid P param) {
        preSave(param);
        E entity = toDO(param);
        boolean ok = baseService.save(entity);
        postSave(entity, ok);
        return Result.success(ok);
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid P param) {
        preUpdate(param);
        E entity = toDO(param);
        entity.setId(id);
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
    public Result<Page<VO>> search(P param, Page<E> page) {
        Page<E> result = baseService.page(page, buildQueryWrapper(param));
        return Result.success(result.convert(this::toVO));
    }

    @GetMapping("/list")
    public Result<List<VO>> getList(P param) {
        List<E> list = baseService.list(buildQueryWrapper(param));
        return Result.success(list.stream().map(this::toVO).collect(Collectors.toList()));
    }

    @GetMapping("/by-ids")
    public Result<List<VO>> getByIds(@RequestParam List<Long> ids) {
        List<E> list = baseService.listByIds(ids);
        return Result.success(list.stream().map(this::toVO).collect(Collectors.toList()));
    }

    @PostMapping("/batch")
    public Result<Boolean> saveBatch(@RequestBody @Valid List<P> paramList) {
        List<E> entities = paramList.stream().map(this::toDO).collect(Collectors.toList());
        return Result.success(baseService.saveBatch(entities));
    }

    @PutMapping("/batch")
    public Result<Boolean> updateBatch(@RequestBody @Valid List<P> paramList) {
        List<E> entities = paramList.stream().map(this::toDO).collect(Collectors.toList());
        return Result.success(baseService.updateBatchById(entities));
    }
}
