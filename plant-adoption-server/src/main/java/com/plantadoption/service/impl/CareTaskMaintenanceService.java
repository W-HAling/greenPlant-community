package com.plantadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plantadoption.entity.CareTask;
import com.plantadoption.entity.Plant;
import com.plantadoption.mapper.CareTaskMapper;
import com.plantadoption.mapper.PlantMapper;
import com.plantadoption.service.NotificationService;
import com.plantadoption.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CareTaskMaintenanceService {

    private final CareTaskMapper careTaskMapper;
    private final PlantMapper plantMapper;
    private final NotificationService notificationService;
    private final SysConfigService sysConfigService;

    public int markOverdueTasks() {
        List<CareTask> tasks = careTaskMapper.selectList(
                new LambdaQueryWrapper<CareTask>()
                        .eq(CareTask::getStatus, "PENDING")
                        .lt(CareTask::getDueDate, LocalDate.now())
        );

        for (CareTask task : tasks) {
            task.setStatus("OVERDUE");
            careTaskMapper.updateById(task);
        }
        return tasks.size();
    }

    public int sendDueReminders() {
        int reminderDays = sysConfigService.getIntConfigValue("care_reminder_days", 1);
        LocalDate deadline = LocalDate.now().plusDays(reminderDays);
        List<CareTask> tasks = careTaskMapper.selectList(
                new LambdaQueryWrapper<CareTask>()
                        .in(CareTask::getStatus, List.of("PENDING", "OVERDUE"))
                        .le(CareTask::getDueDate, deadline)
        );

        int count = 0;
        for (CareTask task : tasks) {
            if (task.getLastRemindTime() != null && task.getLastRemindTime().toLocalDate().isEqual(LocalDate.now())) {
                continue;
            }

            Plant plant = plantMapper.selectById(task.getPlantId());
            String plantName = plant != null ? plant.getName() : "绿植";
            notificationService.sendNotification(
                    task.getAdopterId(),
                    "养护任务提醒",
                    String.format("您有一项%s的养护任务即将到期，请及时处理。", plantName),
                    "CARE",
                    task.getId(),
                    "CareTask"
            );
            task.setLastRemindTime(LocalDateTime.now());
            careTaskMapper.updateById(task);
            count++;
        }
        return count;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void refreshCareTasks() {
        markOverdueTasks();
        sendDueReminders();
    }
}
