package com.plantadoption.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CareTaskAdjustDTO {

    private LocalDate dueDate;

    private String cycleType;

    private Integer cycleValue;

    private String careDetail;

    private String adjustNote;
}
