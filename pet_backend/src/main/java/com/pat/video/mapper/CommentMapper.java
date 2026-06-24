package com.pat.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pat.video.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}