package com.plantadoption.service.impl;

import com.plantadoption.entity.CareTask;
import com.plantadoption.service.NotificationService;
import com.plantadoption.service.SysConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareTaskMaintenanceServiceTest {

    @Mock
    private com.plantadoption.mapper.CareTaskMapper careTaskMapper;

    @Mock
    private com.plantadoption.mapper.PlantMapper plantMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SysConfigService sysConfigService;

    @InjectMocks
    private CareTaskMaintenanceService careTaskMaintenanceService;

    @Test
    void markOverdueTasksShouldUpdatePendingExpiredTasks() {
        CareTask overdueTask = new CareTask();
        overdueTask.setId(11L);
        overdueTask.setStatus("PENDING");
        overdueTask.setDueDate(LocalDate.now().minusDays(1));

        when(careTaskMapper.selectList(any())).thenReturn(List.of(overdueTask));

        int count = careTaskMaintenanceService.markOverdueTasks();

        assertEquals(1, count);
        assertEquals("OVERDUE", overdueTask.getStatus());
        verify(careTaskMapper).updateById(overdueTask);
    }

    @Test
    void sendDueRemindersShouldNotifyTasksDueSoonAndUpdateRemindTime() {
        CareTask task = new CareTask();
        task.setId(21L);
        task.setPlantId(31L);
        task.setAdopterId(41L);
        task.setDueDate(LocalDate.now().plusDays(1));
        task.setStatus("PENDING");

        com.plantadoption.entity.Plant plant = new com.plantadoption.entity.Plant();
        plant.setId(31L);
        plant.setName("绿萝");

        when(sysConfigService.getIntConfigValue("care_reminder_days", 1)).thenReturn(1);
        when(careTaskMapper.selectList(any())).thenReturn(List.of(task));
        when(plantMapper.selectById(31L)).thenReturn(plant);

        int count = careTaskMaintenanceService.sendDueReminders();

        assertEquals(1, count);
        ArgumentCaptor<CareTask> captor = ArgumentCaptor.forClass(CareTask.class);
        verify(careTaskMapper).updateById(captor.capture());
        assertEquals(21L, captor.getValue().getId());
        verify(notificationService).sendNotification(41L, "养护任务提醒", "您有一项绿萝的养护任务即将到期，请及时处理。", "CARE", 21L, "CareTask");
    }

    @Test
    void sendDueRemindersShouldSkipTasksAlreadyRemindedToday() {
        CareTask task = new CareTask();
        task.setId(22L);
        task.setPlantId(32L);
        task.setAdopterId(42L);
        task.setDueDate(LocalDate.now());
        task.setStatus("OVERDUE");
        task.setLastRemindTime(LocalDateTime.now().minusHours(2));

        when(sysConfigService.getIntConfigValue("care_reminder_days", 1)).thenReturn(1);
        when(careTaskMapper.selectList(any())).thenReturn(List.of(task));

        int count = careTaskMaintenanceService.sendDueReminders();

        assertEquals(0, count);
        verify(notificationService, never()).sendNotification(any(), any(), any(), any(), any(), any());
        verify(careTaskMapper, never()).updateById(any());
    }
}
