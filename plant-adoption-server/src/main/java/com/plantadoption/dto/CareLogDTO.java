package com.plantadoption.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 养护日志DTO
 */
@Data
public class CareLogDTO {
    
    @NotNull(message = "绿植ID不能为空")
    private Long plantId;
    
    @NotBlank(message = "养护类型不能为空")
    private String careType;
    
    private String description;
    
    private String images;
}
