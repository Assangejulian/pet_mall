package com.pat.demo.admin.model.domain.Query;

import lombok.Data;

@Data
public class SysDictDataQuery {
    
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private Integer status;
}
