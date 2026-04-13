package com.plantadoption.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发帖DTO
 */
@Data
public class PostDTO {
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多200个字符")
    private String title;
    
    @NotBlank(message = "内容不能为空")
    private String content;
    
    private String postType;
    
    private String images;
}
