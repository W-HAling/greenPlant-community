package com.plantadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.plantadoption.enums.CareType;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 养护日志实体类
 */
@Data
@TableName("care_log")
public class CareLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long plantId;
    
    private String plantName;
    
    private Long userId;
    
    private String userName;
    
    private CareType careType;
    
    private LocalDateTime careTime;
    
    private String description;
    
    private String images;

    private Long careTaskId;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String userAvatar;
    
    @TableField(exist = false)
    private String plantImageUrl;
    
    @TableField(exist = false)
    private String plantLocation;
}
