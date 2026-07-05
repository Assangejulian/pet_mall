package com.pat.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.domain.Result;
import com.pat.video.domain.entity.Comment;
import com.pat.video.service.ICommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "评论管理", description = "评论 CRUD")
@RequestMapping("/api/comment")
public class CommentController {

    private final ICommentService commentService;

    public CommentController(ICommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{id}")
    public Result<Comment> getById(@PathVariable Long id) {
        Comment entity = commentService.getById(id);
        return entity == null ? Result.error("数据不存在") : Result.success(entity);
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody @Valid Comment param) {
        return Result.success(commentService.save(param));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid Comment param) {
        param.setId(id);
        return Result.success(commentService.updateById(param));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(commentService.removeById(id));
    }

    @DeleteMapping("/batch")
    public Result<Boolean> removeBatch(@RequestBody List<Long> ids) {
        return Result.success(commentService.removeByIds(ids));
    }

    @GetMapping("/search")
    public Result<IPage<Comment>> search(Comment param, Page<Comment> page) {
        return Result.success(commentService.page(page, new QueryWrapper<Comment>().orderByDesc("create_time")));
    }

    @GetMapping("/list")
    public Result<List<Comment>> getList(Comment param) {
        return Result.success(commentService.list(new QueryWrapper<Comment>().orderByDesc("create_time")));
    }

    @GetMapping("/by-ids")
    public Result<List<Comment>> getByIds(@RequestParam List<Long> ids) {
        return Result.success(commentService.listByIds(ids));
    }

    @PostMapping("/batch")
    public Result<Boolean> saveBatch(@RequestBody @Valid List<Comment> paramList) {
        return Result.success(commentService.saveBatch(paramList));
    }

    @PutMapping("/batch")
    public Result<Boolean> updateBatch(@RequestBody @Valid List<Comment> paramList) {
        return Result.success(commentService.updateBatchById(paramList));
    }
}
