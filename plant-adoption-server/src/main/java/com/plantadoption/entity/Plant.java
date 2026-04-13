package com.plantadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.plantadoption.enums.PlantStatus;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 绿植实体类
 */
@Data
@TableName("plant")
public class Plant implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String variety;
    
    private String location;
    
    private String imageUrl;
    
    private String qrCodeUrl;
    
    private PlantStatus status;
    
    private Long adopterId;
    
    private LocalDateTime adoptionTime;
    
    private LocalDateTime lastCareTime;
    
    private Integer careCount;
    
    private String description;
    
    private String careTips;

    private Long carePlanTemplateId;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String adopterName;
    
    @TableField(exist = false)
    private String adopterPhone;
    
    @TableField(exist = false)
    private String adopterAvatar;
}
