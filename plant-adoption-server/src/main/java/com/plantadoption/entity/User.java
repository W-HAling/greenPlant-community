package com.plantadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.plantadoption.enums.UserRole;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("sys_user")
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String phone;
    
    private String password;
    
    private String nickname;
    
    private String avatar;
    
    private Long departmentId;
    
    private UserRole role;
    
    private Integer status;
    
    private LocalDateTime lastLoginTime;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String departmentName;
    
    @TableField(exist = false)
    private String token;
}
