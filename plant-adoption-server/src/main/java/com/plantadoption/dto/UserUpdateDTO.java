package com.plantadoption.dto;

import lombok.Data;

/**
 * 用户信息更新DTO
 */
@Data
public class UserUpdateDTO {
    
    private String nickname;
    
    private String avatar;
    
    private Long departmentId;
}
