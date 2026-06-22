package com.pat.demo.common.controller;

import com.artsail.common.domain.Result;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * 逻辑删除控制器基类.
 * 在 BaseController 基础上增加恢复、查找已删除记录等功能.
 * 适用于实体继承 LogicDeleteEntity 的业务.
 *
 * @param <S> Service层接口，需继承IService<T>
 * @param <T> 实体类类型
 * @param <V> VO类型
 * @param <Q> 查询条件对象类型
 */
public abstract class LogicDeleteController<S extends IService<T>, T, V, Q>
        extends BaseController<S, T, V, Q> {

    /**
     * 恢复已逻辑删除的记录.
     * 执行 UPDATE ... SET is_delete = 0 WHERE id = ?
     */
    @PutMapping("/{id}/restore")
    public Result<Boolean> restore(@PathVariable Long id) {
        boolean success = baseService.update(
                new UpdateWrapper<T>()
                        .eq("id", id)
                        .set("is_delete", 0)
        );
        return Result.success(success);
    }

    /**
     * 查找包含已删除记录的分页列表（含逻辑删除的数据）.
     * 子类需要忽略 is_delete = 0 的条件，直接查全部.
     */
    @GetMapping("/search-with-deleted")
    public abstract Result<Page<V>> searchWithDeleted(Page<V> page, Q query);
}