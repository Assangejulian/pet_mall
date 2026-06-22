package com.pat.demo.admin.model.domain;

import com.artsail.common.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_dict_type")
@EqualsAndHashCode(callSuper = true)
public class SysDictType extends BaseEntity {

    private String dictType;
    private String dictName;
    private String dictDesc;
    private Integer status;



}

