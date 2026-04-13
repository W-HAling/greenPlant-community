package com.plantadoption.service.impl;

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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.mapper.CareLogMapper;
import com.plantadoption.mapper.CarePlanItemMapper;
import com.plantadoption.mapper.CareTaskMapper;
import com.plantadoption.mapper.CarePlanTemplateMapper;
import com.plantadoption.mapper.PlantCarePlanMapper;
import com.plantadoption.mapper.PlantMapper;
import com.plantadoption.mapper.UserMapper;
import com.plantadoption.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarePlanServiceImplTest {

    @Mock
    private CarePlanTemplateMapper carePlanTemplateMapper;

    @Mock
    private CarePlanItemMapper carePlanItemMapper;

    @Mock
    private PlantMapper plantMapper;

    @Mock
    private PlantCarePlanMapper plantCarePlanMapper;

    @Mock
    private CareTaskMapper careTaskMapper;

    @Mock
    private CareLogMapper careLogMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CareTaskMaintenanceService careTaskMaintenanceService;

    @InjectMocks
    private CarePlanServiceImpl carePlanService;

    @Test
    void createTemplateShouldPersistTemplateAndItems() {
        CarePlanTemplateDTO dto = new CarePlanTemplateDTO();
        dto.setTemplateName("绿萝标准模板");
        dto.setPlantCategory("foliage");
        dto.setDescription("适用于办公室常见绿萝");

        CarePlanItemDTO water = new CarePlanItemDTO();
        water.setCareType("water");
        water.setCycleType("weekly");
        water.setCycleValue(1);
        water.setCareDetail("每周浇水一次");

        CarePlanItemDTO clean = new CarePlanItemDTO();
        clean.setCareType("clean");
        clean.setCycleType("weekly");
        clean.setCycleValue(2);
        clean.setCareDetail("每两周擦拭叶片");

        dto.setItems(List.of(water, clean));

        doAnswer(invocation -> {
            CarePlanTemplate template = invocation.getArgument(0);
            template.setId(101L);
            return 1;
        }).when(carePlanTemplateMapper).insert(any(CarePlanTemplate.class));

        CarePlanTemplate created = carePlanService.createTemplate(dto, 7L);

        assertNotNull(created);
        assertEquals(101L, created.getId());
        assertEquals("绿萝标准模板", created.getTemplateName());
        assertEquals(2, created.getItems().size());

        ArgumentCaptor<CarePlanItem> itemCaptor = ArgumentCaptor.forClass(CarePlanItem.class);
        verify(carePlanItemMapper, times(2)).insert(itemCaptor.capture());
        List<CarePlanItem> insertedItems = itemCaptor.getAllValues();
        assertEquals(101L, insertedItems.get(0).getTemplateId());
        assertEquals(101L, insertedItems.get(1).getTemplateId());
    }

    @Test
    void bindPlantPlanShouldCreateInstanceAndInitialTasks() {
        Plant plant = new Plant();
        plant.setId(9L);
        plant.setName("绿萝");
        plant.setAdopterId(18L);
        plant.setCarePlanTemplateId(101L);
        when(plantMapper.selectById(9L)).thenReturn(plant);

        CarePlanItem item = new CarePlanItem();
        item.setId(201L);
        item.setTemplateId(101L);
        item.setCycleType("weekly");
        item.setCycleValue(1);
        item.setCareType("water");
        when(carePlanItemMapper.selectList(any())).thenReturn(List.of(item));

        doAnswer(invocation -> {
            PlantCarePlan plan = invocation.getArgument(0);
            plan.setId(301L);
            return 1;
        }).when(plantCarePlanMapper).insert(any(PlantCarePlan.class));
        doAnswer(invocation -> {
            CareTask task = invocation.getArgument(0);
            task.setId(401L);
            return 1;
        }).when(careTaskMapper).insert(any(CareTask.class));

        carePlanService.bindPlantPlan(9L, 101L, "初始绑定");

        ArgumentCaptor<CareTask> taskCaptor = ArgumentCaptor.forClass(CareTask.class);
        verify(careTaskMapper).insert(taskCaptor.capture());
        assertEquals(301L, taskCaptor.getValue().getPlantCarePlanId());
        assertEquals(18L, taskCaptor.getValue().getAdopterId());
        assertEquals("PENDING", taskCaptor.getValue().getStatus());
        verify(notificationService).sendNotification(18L, "养护任务已生成", "您领养的绿萝已生成1项养护任务，请及时查看。", "CARE", 401L, "CareTask");
    }

    @Test
    void executeTaskShouldCreateCareLogAndCompleteTask() {
        CareTask task = new CareTask();
        task.setId(401L);
        task.setPlantCarePlanId(301L);
        task.setPlantId(9L);
        task.setAdopterId(18L);
        task.setPlanItemId(201L);
        task.setStatus("PENDING");
        when(careTaskMapper.selectById(401L)).thenReturn(task);

        CarePlanItem item = new CarePlanItem();
        item.setId(201L);
        item.setCareType("water");
        item.setCycleType("weekly");
        item.setCycleValue(1);
        item.setCareDetail("浇水 50ml");
        when(carePlanItemMapper.selectById(201L)).thenReturn(item);

        Plant plant = new Plant();
        plant.setId(9L);
        plant.setName("绿萝");
        when(plantMapper.selectById(9L)).thenReturn(plant);

        User user = new User();
        user.setId(18L);
        user.setNickname("小王");
        when(userMapper.selectById(18L)).thenReturn(user);

        doAnswer(invocation -> {
            CareLog careLog = invocation.getArgument(0);
            careLog.setId(501L);
            return 1;
        }).when(careLogMapper).insert(any(CareLog.class));
        doAnswer(invocation -> {
            CareTask nextTask = invocation.getArgument(0);
            nextTask.setId(601L);
            return 1;
        }).when(careTaskMapper).insert(any(CareTask.class));

        CareTaskExecuteDTO dto = new CareTaskExecuteDTO();
        dto.setDescription("按计划完成浇水");
        dto.setImages("[\"a.png\"]");

        CareLog log = carePlanService.executeTask(18L, 401L, dto);

        assertNotNull(log);
        assertEquals(501L, log.getId());
        assertEquals("DONE", task.getStatus());
        assertEquals(501L, task.getCareLogId());
        verify(careTaskMapper).updateById(task);
        ArgumentCaptor<CareTask> nextTaskCaptor = ArgumentCaptor.forClass(CareTask.class);
        verify(careTaskMapper, times(1)).insert(nextTaskCaptor.capture());
        assertEquals(301L, nextTaskCaptor.getValue().getPlantCarePlanId());
        assertEquals("PENDING", nextTaskCaptor.getValue().getStatus());
        assertNull(nextTaskCaptor.getValue().getCareDetailOverride());
    }

    @Test
    void pageTasksShouldReturnPagedAdminOverview() {
        Page<CareTask> page = new Page<>(1, 10);
        CareTask task = new CareTask();
        task.setId(701L);
        task.setPlantName("绿萝");
        Page<CareTask> result = new Page<>(1, 10);
        result.setRecords(List.of(task));
        result.setTotal(1);

        when(careTaskMapper.selectAdminTasks(page, "PENDING", "绿萝")).thenReturn(result);

        Page<CareTask> actual = (Page<CareTask>) carePlanService.pageTasks(page, "PENDING", "绿萝");

        assertEquals(1, actual.getTotal());
        assertEquals("绿萝", actual.getRecords().get(0).getPlantName());
        verify(careTaskMaintenanceService).markOverdueTasks();
    }

    @Test
    void adjustTaskShouldUpdatePendingTaskOverridesOnly() {
        CareTask task = new CareTask();
        task.setId(801L);
        task.setPlantCarePlanId(301L);
        task.setStatus("PENDING");
        when(careTaskMapper.selectById(801L)).thenReturn(task);

        CareTaskAdjustDTO dto = new CareTaskAdjustDTO();
        dto.setCycleType("daily");
        dto.setCycleValue(3);
        dto.setCareDetail("改为少量浇水");
        dto.setAdjustNote("南窗日照增强");

        CareTask adjusted = carePlanService.adjustTask(801L, dto);

        assertEquals("daily", adjusted.getCycleTypeOverride());
        assertEquals(3, adjusted.getCycleValueOverride());
        assertEquals("改为少量浇水", adjusted.getCareDetailOverride());
        verify(careTaskMapper).updateById(task);
    }

    @Test
    void executeTaskShouldSkipCreatingNextTaskWhenActiveTaskAlreadyExists() {
        CareTask task = new CareTask();
        task.setId(901L);
        task.setPlantCarePlanId(301L);
        task.setPlantId(9L);
        task.setAdopterId(18L);
        task.setPlanItemId(201L);
        task.setStatus("PENDING");
        when(careTaskMapper.selectById(901L)).thenReturn(task);

        CarePlanItem item = new CarePlanItem();
        item.setId(201L);
        item.setCareType("water");
        item.setCycleType("weekly");
        item.setCycleValue(1);
        item.setCareDetail("浇水 50ml");
        when(carePlanItemMapper.selectById(201L)).thenReturn(item);

        Plant plant = new Plant();
        plant.setId(9L);
        plant.setName("绿萝");
        when(plantMapper.selectById(9L)).thenReturn(plant);

        User user = new User();
        user.setId(18L);
        user.setNickname("小王");
        when(userMapper.selectById(18L)).thenReturn(user);

        doAnswer(invocation -> {
            CareLog careLog = invocation.getArgument(0);
            careLog.setId(902L);
            return 1;
        }).when(careLogMapper).insert(any(CareLog.class));

        when(careTaskMapper.selectCount(any())).thenReturn(1L);

        CareTaskExecuteDTO dto = new CareTaskExecuteDTO();
        dto.setDescription("完成本次浇水");

        carePlanService.executeTask(18L, 901L, dto);

        verify(careTaskMapper, times(0)).insert(any(CareTask.class));
    }

    @Test
    void getTaskStatsShouldReturnSummaryCounts() {
        when(careTaskMapper.selectCount(any()))
                .thenReturn(10L)
                .thenReturn(4L)
                .thenReturn(2L)
                .thenReturn(4L);

        CareTaskStatsDTO stats = carePlanService.getTaskStats();

        assertEquals(10L, stats.getTotal());
        assertEquals(4L, stats.getPending());
        assertEquals(2L, stats.getOverdue());
        assertEquals(4L, stats.getDone());
        verify(careTaskMaintenanceService).markOverdueTasks();
    }
}
