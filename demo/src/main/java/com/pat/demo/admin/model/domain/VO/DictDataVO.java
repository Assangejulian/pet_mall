package com.pat.demo.admin.model.domain.VO;

import lombok.Data;

@Data
public class DictDataVO {
    
    /**
     * 字典标签（显示值）
     */
    private String label;
    
    /**
     * 字典值（存储值）
     */
    private String value;
}
