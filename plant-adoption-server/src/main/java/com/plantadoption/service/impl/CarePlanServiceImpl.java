package com.plantadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.common.ErrorCode;
import com.plantadoption.dto.CarePlanItemDTO;
import com.plantadoption.dto.CareTaskAdjustDTO;
import com.plantadoption.dto.CareTaskExecuteDTO;
import com.plantadoption.dto.CareTaskStatsDTO;
import com.plantadoption.dto.CarePlanTemplateDTO;
import com.plantadoption.entity.CareLog;
import com.plantadoption.entity.CarePlanItem;
import com.plantadoption.entity.CareTask;
import com.plantadoption.entity.CarePlanTemplate;
import com.plantadoption.entity.Plant;
import com.plantadoption.entity.PlantCarePlan;
import com.plantadoption.entity.User;
import com.plantadoption.enums.CareType;
import com.plantadoption.exception.BusinessException;
import com.plantadoption.mapper.CareLogMapper;
import com.plantadoption.mapper.CarePlanItemMapper;
import com.plantadoption.mapper.CareTaskMapper;
import com.plantadoption.mapper.CarePlanTemplateMapper;
import com.plantadoption.mapper.PlantCarePlanMapper;
import com.plantadoption.mapper.PlantMapper;
import com.plantadoption.mapper.UserMapper;
import com.plantadoption.service.CarePlanService;
import com.plantadoption.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarePlanServiceImpl implements CarePlanService {

    private final CarePlanTemplateMapper carePlanTemplateMapper;
    private final CarePlanItemMapper carePlanItemMapper;
    private final PlantMapper plantMapper;
    private final PlantCarePlanMapper plantCarePlanMapper;
    private final CareTaskMapper careTaskMapper;
    private final CareLogMapper careLogMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final CareTaskMaintenanceService careTaskMaintenanceService;

    @Override
    @Transactional
    public CarePlanTemplate createTemplate(CarePlanTemplateDTO dto, Long operatorId) {
        if (dto == null || CollectionUtils.isEmpty(dto.getItems())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        CarePlanTemplate template = new CarePlanTemplate();
        template.setTemplateName(dto.getTemplateName());
        template.setPlantCategory(dto.getPlantCategory());
        template.setPlantSpecies(dto.getPlantSpecies());
        template.setDescription(dto.getDescription());
        template.setCreateBy(operatorId);
        template.setStatus(1);
        carePlanTemplateMapper.insert(template);

        List<CarePlanItem> items = new ArrayList<>();
        for (CarePlanItemDTO itemDTO : dto.getItems()) {
            CarePlanItem item = new CarePlanItem();
            item.setTemplateId(template.getId());
            item.setCareType(itemDTO.getCareType());
            item.setCycleType(itemDTO.getCycleType());
            item.setCycleValue(itemDTO.getCycleValue());
            item.setCareDetail(itemDTO.getCareDetail());
            item.setCareNote(itemDTO.getCareNote());
            item.setRemindAdvance(itemDTO.getRemindAdvance() != null ? itemDTO.getRemindAdvance() : 1);
            item.setSort(itemDTO.getSort() != null ? itemDTO.getSort() : 0);
            carePlanItemMapper.insert(item);
            items.add(item);
        }

        template.setItems(items);
        return template;
    }

    @Override
    public List<CarePlanTemplate> listTemplates(String plantCategory, Integer status) {
        LambdaQueryWrapper<CarePlanTemplate> wrapper = new LambdaQueryWrapper<>();
        if (plantCategory != null && !plantCategory.isBlank()) {
            wrapper.eq(CarePlanTemplate::getPlantCategory, plantCategory);
        }
        if (status != null) {
            wrapper.eq(CarePlanTemplate::getStatus, status);
        }
        wrapper.orderByDesc(CarePlanTemplate::getCreateTime);

        List<CarePlanTemplate> templates = carePlanTemplateMapper.selectList(wrapper);
        for (CarePlanTemplate template : templates) {
            List<CarePlanItem> items = carePlanItemMapper.selectList(
                    new LambdaQueryWrapper<CarePlanItem>()
                            .eq(CarePlanItem::getTemplateId, template.getId())
                            .orderByAsc(CarePlanItem::getSort)
                            .orderByAsc(CarePlanItem::getId)
            );
            template.setItems(items);
        }
        return templates;
    }

    @Override
    @Transactional
    public void bindPlantPlan(Long plantId, Long templateId, String adjustNote) {
        Plant plant = plantMapper.selectById(plantId);
        if (plant == null) {
            throw new BusinessException(ErrorCode.PLANT_NOT_FOUND);
        }
        if (plant.getAdopterId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前绿植还没有领养人，无法生成任务");
        }

        PlantCarePlan plan = new PlantCarePlan();
        plan.setPlantId(plantId);
        plan.setTemplateId(templateId);
        plan.setAdopterId(plant.getAdopterId());
        plan.setAdjustNote(adjustNote);
        plantCarePlanMapper.insert(plan);

        List<CarePlanItem> items = carePlanItemMapper.selectList(
                new LambdaQueryWrapper<CarePlanItem>()
                        .eq(CarePlanItem::getTemplateId, templateId)
                        .orderByAsc(CarePlanItem::getSort)
                        .orderByAsc(CarePlanItem::getId)
        );

        Long firstTaskId = null;
        for (CarePlanItem item : items) {
            CareTask task = new CareTask();
            task.setPlantCarePlanId(plan.getId());
            task.setPlanItemId(item.getId());
            task.setPlantId(plantId);
            task.setAdopterId(plant.getAdopterId());
            task.setDueDate(calculateDueDate(item));
            task.setStatus("PENDING");
            careTaskMapper.insert(task);
            if (firstTaskId == null) {
                firstTaskId = task.getId();
            }
        }

        if (!items.isEmpty()) {
            notificationService.sendNotification(
                    plant.getAdopterId(),
                    "养护任务已生成",
                    String.format("您领养的%s已生成%d项养护任务，请及时查看。", plant.getName(), items.size()),
                    "CARE",
                    firstTaskId != null ? firstTaskId : plantId,
                    "CareTask"
            );
        }
    }

    @Override
    public List<CareTask> listMyTasks(Long userId, String status) {
        careTaskMaintenanceService.markOverdueTasks();
        return careTaskMapper.selectMyTasks(userId, status);
    }

    @Override
    public IPage<CareTask> pageTasks(Page<CareTask> page, String status, String keyword) {
        careTaskMaintenanceService.markOverdueTasks();
        return careTaskMapper.selectAdminTasks(page, status, keyword);
    }

    @Override
    public CareTaskStatsDTO getTaskStats() {
        careTaskMaintenanceService.markOverdueTasks();
        CareTaskStatsDTO stats = new CareTaskStatsDTO();
        stats.setTotal(careTaskMapper.selectCount(new LambdaQueryWrapper<>()));
        stats.setPending(careTaskMapper.selectCount(new LambdaQueryWrapper<CareTask>().eq(CareTask::getStatus, "PENDING")));
        stats.setOverdue(careTaskMapper.selectCount(new LambdaQueryWrapper<CareTask>().eq(CareTask::getStatus, "OVERDUE")));
        stats.setDone(careTaskMapper.selectCount(new LambdaQueryWrapper<CareTask>().eq(CareTask::getStatus, "DONE")));
        return stats;
    }

    @Override
    @Transactional
    public CareTask adjustTask(Long taskId, CareTaskAdjustDTO dto) {
        CareTask task = careTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "养护任务不存在");
        }
        if (!"PENDING".equalsIgnoreCase(task.getStatus()) && !"OVERDUE".equalsIgnoreCase(task.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前任务状态不可调整");
        }

        if (dto != null) {
            if (dto.getDueDate() != null) {
                task.setDueDate(dto.getDueDate());
            }
            if (dto.getCycleType() != null && !dto.getCycleType().isBlank()) {
                task.setCycleTypeOverride(dto.getCycleType().trim());
            }
            if (dto.getCycleValue() != null) {
                task.setCycleValueOverride(dto.getCycleValue());
            }
            if (dto.getCareDetail() != null && !dto.getCareDetail().isBlank()) {
                task.setCareDetailOverride(dto.getCareDetail().trim());
            }
            if (dto.getAdjustNote() != null && !dto.getAdjustNote().isBlank()) {
                PlantCarePlan plan = plantCarePlanMapper.selectById(task.getPlantCarePlanId());
                if (plan != null) {
                    plan.setAdjustNote(dto.getAdjustNote().trim());
                    plantCarePlanMapper.updateById(plan);
                }
            }
        }

        careTaskMapper.updateById(task);
        return task;
    }

    @Override
    @Transactional
    public CareLog executeTask(Long userId, Long taskId, CareTaskExecuteDTO dto) {
        CareTask task = careTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "养护任务不存在");
        }
        if (!userId.equals(task.getAdopterId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!"PENDING".equalsIgnoreCase(task.getStatus()) && !"OVERDUE".equalsIgnoreCase(task.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前任务不可执行");
        }

        CarePlanItem item = carePlanItemMapper.selectById(task.getPlanItemId());
        Plant plant = plantMapper.selectById(task.getPlantId());
        User user = userMapper.selectById(userId);

        CareLog careLog = new CareLog();
        careLog.setPlantId(task.getPlantId());
        careLog.setPlantName(plant.getName());
        careLog.setUserId(userId);
        careLog.setUserName(user.getNickname());
        careLog.setCareType(parseCareType(item.getCareType()));
        careLog.setCareTime(LocalDateTime.now());
        careLog.setDescription(dto != null ? dto.getDescription() : null);
        careLog.setImages(dto != null ? dto.getImages() : null);
        careLog.setCareTaskId(task.getId());
        careLogMapper.insert(careLog);

        task.setStatus("DONE");
        task.setCompletedTime(LocalDateTime.now());
        task.setCareLogId(careLog.getId());
        careTaskMapper.updateById(task);

        createNextTask(task, item);

        plantMapper.incrementCareCount(task.getPlantId());
        return careLog;
    }

    private void createNextTask(CareTask task, CarePlanItem item) {
        Long activeCount = careTaskMapper.selectCount(
                new LambdaQueryWrapper<CareTask>()
                        .eq(CareTask::getPlantCarePlanId, task.getPlantCarePlanId())
                        .eq(CareTask::getPlanItemId, task.getPlanItemId())
                        .eq(CareTask::getAdopterId, task.getAdopterId())
                        .in(CareTask::getStatus, List.of("PENDING", "OVERDUE"))
        );
        if (activeCount != null && activeCount > 0) {
            return;
        }

        CareTask nextTask = new CareTask();
        nextTask.setPlantCarePlanId(task.getPlantCarePlanId());
        nextTask.setPlanItemId(task.getPlanItemId());
        nextTask.setPlantId(task.getPlantId());
        nextTask.setAdopterId(task.getAdopterId());
        nextTask.setStatus("PENDING");
        nextTask.setCycleTypeOverride(task.getCycleTypeOverride());
        nextTask.setCycleValueOverride(task.getCycleValueOverride());
        nextTask.setCareDetailOverride(task.getCareDetailOverride());
        nextTask.setDueDate(calculateNextDueDate(task, item));
        careTaskMapper.insert(nextTask);
    }

    private LocalDate calculateNextDueDate(CareTask task, CarePlanItem item) {
        Integer cycleValue = task.getCycleValueOverride() != null ? task.getCycleValueOverride() : item.getCycleValue();
        String cycleType = task.getCycleTypeOverride() != null ? task.getCycleTypeOverride() : item.getCycleType();
        LocalDate baseDate = LocalDate.now();
        if (cycleValue == null) {
            return baseDate.plusDays(1);
        }
        return switch (cycleType == null ? "" : cycleType.toLowerCase()) {
            case "daily" -> baseDate.plusDays(cycleValue);
            case "monthly" -> baseDate.plusMonths(cycleValue);
            default -> baseDate.plusWeeks(cycleValue);
        };
    }

    private LocalDate calculateDueDate(CarePlanItem item) {
        if (item == null || item.getCycleValue() == null) {
            return LocalDate.now().plusDays(1);
        }
        return switch (item.getCycleType() == null ? "" : item.getCycleType().toLowerCase()) {
            case "daily" -> LocalDate.now().plusDays(item.getCycleValue());
            case "monthly" -> LocalDate.now().plusMonths(item.getCycleValue());
            default -> LocalDate.now().plusWeeks(item.getCycleValue());
        };
    }

    private CareType parseCareType(String careType) {
        if (careType == null || careType.isBlank()) {
            return CareType.OTHER;
        }
        return switch (careType.toUpperCase()) {
            case "WATER" -> CareType.WATER;
            case "FERTILIZE" -> CareType.FERTILIZE;
            case "PRUNE" -> CareType.PRUNE;
            default -> CareType.OTHER;
        };
    }
}
