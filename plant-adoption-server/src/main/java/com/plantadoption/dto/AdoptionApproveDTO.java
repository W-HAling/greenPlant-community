package com.plantadoption.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 领养审批DTO
 */
@Data
public class AdoptionApproveDTO {
    
    @NotNull(message = "记录ID不能为空")
    private Long recordId;
    
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;
    
    private String remark;
}
