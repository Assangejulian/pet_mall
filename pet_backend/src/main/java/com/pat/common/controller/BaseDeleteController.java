package com.pat.common.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.common.domain.BaseEntity;
import com.pat.common.domain.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * 逻辑删除控制器基类。
 *
 * <p>适用于使用 MyBatis-Plus @TableLogic 的 DO。提供默认的恢复和已删查询实现，
 * 特殊业务（如级联恢复）自行 override。</p>
 *
 * @param <E>  DO / Entity，必须继承 BaseEntity
 * @param <P>  Param，接口入参，合并 DTO 和 Query
 * @param <VO> VO，接口返回对象
 */
public abstract class BaseDeleteController<E extends BaseEntity, P, VO>
        extends BaseController<E, P, VO> {

    protected BaseDeleteController(IService<E> baseService) {
        super(baseService);
    }

    /** 恢复已逻辑删除记录。默认通过 mapper 直接 set deleted=0 绕过 @TableLogic 过滤。 */
    @PutMapping("/{id}/restore")
    public Result<Boolean> restore(@PathVariable Long id) {
        int rows = baseService.getBaseMapper().update(null,
                new UpdateWrapper<E>().eq("id", id).set("deleted", 0)
        );
        return Result.success(rows > 0);
    }

    /** 分页查询含已删除记录。默认直接透传 page 参数，如需绕过 @TableLogic 过滤则 override。 */
    @GetMapping("/search-with-deleted")
    public Result<Page<E>> searchWithDeleted(Page<E> page, P param) {
        return Result.success(baseService.page(page, buildQueryWrapper(param)));
    }
}
