package com.plantadoption.dto;

import lombok.Data;

@Data
public class CareTaskStatsDTO {

    private Long total;

    private Long pending;

    private Long overdue;

    private Long done;
}
