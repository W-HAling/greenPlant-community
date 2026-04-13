package com.plantadoption.service.impl;

import com.plantadoption.dto.AdoptionApproveDTO;
import com.plantadoption.entity.AdoptionRecord;
import com.plantadoption.entity.Plant;
import com.plantadoption.entity.User;
import com.plantadoption.enums.AdoptionStatus;
import com.plantadoption.enums.PlantStatus;
import com.plantadoption.mapper.AdoptionRecordMapper;
import com.plantadoption.mapper.PlantMapper;
import com.plantadoption.mapper.UserMapper;
import com.plantadoption.service.CarePlanService;
import com.plantadoption.service.NotificationService;
import com.plantadoption.service.SysConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdoptionServiceImplTest {

    @Mock
    private AdoptionRecordMapper adoptionRecordMapper;

    @Mock
    private PlantMapper plantMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SysConfigService sysConfigService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CarePlanService carePlanService;

    @InjectMocks
    private AdoptionServiceImpl adoptionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adoptionService, "baseMapper", adoptionRecordMapper);
    }

    @Test
    void approveShouldBindCarePlanWhenPlantHasTemplate() {
        AdoptionRecord record = new AdoptionRecord();
        record.setId(11L);
        record.setPlantId(21L);
        record.setPlantName("绿萝");
        record.setUserId(31L);
        record.setStatus(AdoptionStatus.PENDING);
        when(adoptionRecordMapper.selectById(11L)).thenReturn(record);

        User approver = new User();
        approver.setId(99L);
        approver.setNickname("管理员");
        when(userMapper.selectById(99L)).thenReturn(approver);

        Plant plant = new Plant();
        plant.setId(21L);
        plant.setStatus(PlantStatus.AVAILABLE);
        plant.setCarePlanTemplateId(51L);
        when(plantMapper.selectById(21L)).thenReturn(plant);

        AdoptionApproveDTO dto = new AdoptionApproveDTO();
        dto.setRecordId(11L);
        dto.setApproved(true);
        dto.setRemark("通过");

        adoptionService.approve(99L, dto);

        verify(carePlanService).bindPlantPlan(21L, 51L, "领养审批通过自动绑定");
    }

    @Test
    void approveShouldSkipBindingWhenPlantHasNoTemplate() {
        AdoptionRecord record = new AdoptionRecord();
        record.setId(12L);
        record.setPlantId(22L);
        record.setPlantName("发财树");
        record.setUserId(32L);
        record.setStatus(AdoptionStatus.PENDING);
        when(adoptionRecordMapper.selectById(12L)).thenReturn(record);

        User approver = new User();
        approver.setId(100L);
        approver.setNickname("管理员");
        when(userMapper.selectById(100L)).thenReturn(approver);

        Plant plant = new Plant();
        plant.setId(22L);
        plant.setStatus(PlantStatus.AVAILABLE);
        plant.setCarePlanTemplateId(null);
        plant.setAdoptionTime(LocalDateTime.now());
        when(plantMapper.selectById(22L)).thenReturn(plant);

        AdoptionApproveDTO dto = new AdoptionApproveDTO();
        dto.setRecordId(12L);
        dto.setApproved(true);

        adoptionService.approve(100L, dto);

        verify(carePlanService, never()).bindPlantPlan(22L, null, "领养审批通过自动绑定");
    }
}
