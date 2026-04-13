package com.plantadoption.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 养护计划项 DTO
 */
@Data
public class CarePlanItemDTO {

    @NotBlank(message = "养护类型不能为空")
    private String careType;

    @NotBlank(message = "周期类型不能为空")
    private String cycleType;

    @NotNull(message = "周期值不能为空")
    private Integer cycleValue;

    @NotBlank(message = "养护细节不能为空")
    private String careDetail;

    private String careNote;

    private Integer remindAdvance;

    private Integer sort;
}
