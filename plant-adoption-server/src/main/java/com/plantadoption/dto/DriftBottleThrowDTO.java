package com.plantadoption.dto;

import lombok.Data;

import java.util.List;

/**
 * 漂流瓶发送 DTO
 */
@Data
public class DriftBottleThrowDTO {

    private String content;

    private List<String> imageUrls;
}
