package com.pat.common.controller;

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
 * <p>适用于使用 MyBatis-Plus @TableLogic 的 DO。恢复逻辑需要由具体业务实现，
 * 避免通用 update 被逻辑删除条件过滤后无法匹配已删除记录。</p>
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

    /** 恢复已逻辑删除记录，具体业务负责绕过 @TableLogic 自动过滤。 */
    @PutMapping("/{id}/restore")
    public abstract Result<Boolean> restore(@PathVariable Long id);

    /** 分页查询含已删除记录，具体业务负责绕过 @TableLogic 自动过滤。 */
    @GetMapping("/search-with-deleted")
    public abstract Result<Page<VO>> searchWithDeleted(Page<VO> page, P param);
}
