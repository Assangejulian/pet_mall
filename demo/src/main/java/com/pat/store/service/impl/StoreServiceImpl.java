package com.pat.store.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.store.entity.Store;
import com.pat.store.mapper.StoreMapper;
import com.pat.store.service.IStoreService;
import org.springframework.stereotype.Service;

@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements IStoreService {
}