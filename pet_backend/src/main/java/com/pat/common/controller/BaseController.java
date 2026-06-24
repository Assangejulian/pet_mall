package com.pat.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.common.domain.BaseEntity;
import com.pat.common.domain.Result;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
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
    protected boolean doSave(E entity, P param) {
        return baseService.save(entity);
    }
    protected boolean doUpdate(Long id, E entity, P param) {
        return baseService.updateById(entity);
    }
    protected boolean doRemove(Long id) {
        return baseService.removeById(id);
    }

    @GetMapping("/{id}")
    public Result<VO> getById(@PathVariable Long id) {
        E entity = baseService.getById(id);
        return entity == null ? Result.error("数据不存在") : Result.success(toVO(entity));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody @Valid P param) {
        preSave(param);
        E entity = toDO(param);
        boolean ok = doSave(entity, param);
        postSave(entity, ok);
        return Result.success(ok);
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid P param) {
        preUpdate(param);
        E entity = toDO(param);
        entity.setId(id);
        boolean ok = doUpdate(id, entity, param);
        postUpdate(entity, ok);
        return Result.success(ok);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(doRemove(id));
    }

    @DeleteMapping("/batch")
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> removeBatch(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            if (!doRemove(id)) {
                return Result.success(false);
            }
        }
        return Result.success(true);
    }

    @GetMapping("/search")
    public Result<IPage<VO>> search(P param, Page<E> page) {
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
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> saveBatch(@RequestBody @Valid List<P> paramList) {
        for (P param : paramList) {
            preSave(param);
            E entity = toDO(param);
            boolean ok = doSave(entity, param);
            postSave(entity, ok);
            if (!ok) {
                return Result.success(false);
            }
        }
        return Result.success(true);
    }

    @PutMapping("/batch")
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateBatch(@RequestBody @Valid List<P> paramList) {
        for (P param : paramList) {
            preUpdate(param);
            E entity = toDO(param);
            boolean ok = doUpdate(entity.getId(), entity, param);
            postUpdate(entity, ok);
            if (!ok) {
                return Result.success(false);
            }
        }
        return Result.success(true);
    }
}
