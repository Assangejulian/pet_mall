package com.pat.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.common.controller.BaseController;
import com.pat.video.domain.entity.Comment;
import com.pat.video.service.ICommentService;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "评论管理", description = "评论 CRUD")
@RequestMapping("/api/comment")
public class CommentController extends BaseController<Comment, Comment, Comment> {

    public CommentController(ICommentService service) {
        super(service);
    }

    @Override
    protected Comment toVO(Comment entity) {
        return entity;
    }

    @Override
    protected Comment toDO(Comment param) {
        return param;
    }

    @Override
    protected QueryWrapper<Comment> buildQueryWrapper(Comment param) {
        return new QueryWrapper<>();
    }
}