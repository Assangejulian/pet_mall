package com.pat.common.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.common.domain.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * 逻辑删除控制器基类，在 {@link BaseController} 基础上增加恢复 &amp; 含已删除记录查询。
 *
 * <p>适用条件：Entity 使用了 MyBatis-Plus {@code @TableLogic} 逻辑删除。</p>
 *
 * @param <S> Service，需继承 IService&lt;E&gt;
 * @param <E> Entity，需有 isDelete 字段并配置 @TableLogic
 * @param <V> VO
 * @param <Q> Query
 */
public abstract class BaseDeleteController<S extends IService<E>, E, V, Q>
        extends BaseController<S, E, V, Q> {

    /** 恢复逻辑删除的记录（UPDATE SET is_delete = 0） */
    @PutMapping("/{id}/restore")
    public Result<Boolean> restore(@PathVariable Long id) {
        boolean ok = baseService.update(
                new UpdateWrapper<E>().eq("id", id).set("is_delete", 0));
        return Result.success(ok);
    }

    /** 分页查询含已删除记录（子类实现，需绕过 @TableLogic 自动过滤） */
    @GetMapping("/search-with-deleted")
    public abstract Result<Page<V>> searchWithDeleted(Page<V> page, Q query);
}