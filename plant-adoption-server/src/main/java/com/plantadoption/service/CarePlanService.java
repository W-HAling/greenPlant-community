package com.plantadoption.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.dto.CareTaskAdjustDTO;
import com.plantadoption.dto.CareTaskExecuteDTO;
import com.plantadoption.dto.CareTaskStatsDTO;
import com.plantadoption.entity.CareLog;
import com.plantadoption.entity.CareTask;
import com.plantadoption.dto.CarePlanTemplateDTO;
import com.plantadoption.entity.CarePlanTemplate;

import java.util.List;

public interface CarePlanService {

    CarePlanTemplate createTemplate(CarePlanTemplateDTO dto, Long operatorId);

    List<CarePlanTemplate> listTemplates(String plantCategory, Integer status);

    void bindPlantPlan(Long plantId, Long templateId, String adjustNote);

    List<CareTask> listMyTasks(Long userId, String status);

    IPage<CareTask> pageTasks(Page<CareTask> page, String status, String keyword);

    CareTaskStatsDTO getTaskStats();

    CareTask adjustTask(Long taskId, CareTaskAdjustDTO dto);

    CareLog executeTask(Long userId, Long taskId, CareTaskExecuteDTO dto);
}
