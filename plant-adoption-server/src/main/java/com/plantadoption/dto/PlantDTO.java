package com.plantadoption.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 绿植DTO
 */
@Data
public class PlantDTO {
    
    @NotBlank(message = "绿植名称不能为空")
    @Size(max = 100, message = "名称最多100个字符")
    private String name;
    
    private String variety;
    
    private String location;
    
    private String imageUrl;
    
    private String description;
    
    private String careTips;

    private Long carePlanTemplateId;
}
