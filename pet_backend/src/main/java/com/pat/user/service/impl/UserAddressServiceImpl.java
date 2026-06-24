package com.pat.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.user.entity.UserAddress;
import com.pat.user.mapper.UserAddressMapper;
import com.pat.user.service.IUserAddressService;
import org.springframework.stereotype.Service;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements IUserAddressService {
}