package com.pat.demo.admin.model.domain.Query;

import com.artsail.common.domain.BaseEntity;
import lombok.Data;

@Data
public class SysDictTypeQuery  extends BaseEntity {
    
    private String dictType;
    private String dictName;
    private Integer status;
}
