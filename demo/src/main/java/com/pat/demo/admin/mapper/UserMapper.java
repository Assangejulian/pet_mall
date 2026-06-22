package com.pat.demo.admin.mapper;

import com.artsail.admin.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 13372
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2026-03-12 17:19:06
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {


}




