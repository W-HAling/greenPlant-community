package com.plantadoption.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 领养申请DTO
 */
@Data
public class AdoptionApplyDTO {
    
    @NotNull(message = "绿植ID不能为空")
    private Long plantId;
    
    private String remark;
}
