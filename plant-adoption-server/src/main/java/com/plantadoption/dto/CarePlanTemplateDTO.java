package com.plantadoption.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 养护模板 DTO
 */
@Data
public class CarePlanTemplateDTO {

    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    private String plantCategory;

    private String plantSpecies;

    private String description;

    @Valid
    @NotEmpty(message = "至少需要一个养护计划项")
    private List<CarePlanItemDTO> items;
}
