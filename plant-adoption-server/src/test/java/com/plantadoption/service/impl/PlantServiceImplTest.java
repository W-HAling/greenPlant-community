package com.plantadoption.service.impl;

import com.plantadoption.dto.PlantDTO;
import com.plantadoption.entity.Plant;
import com.plantadoption.mapper.PlantMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlantServiceImplTest {

    @Mock
    private PlantMapper plantMapper;

    @InjectMocks
    private PlantServiceImpl plantService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(plantService, "baseMapper", plantMapper);
    }

    @Test
    void createPlantShouldPersistCarePlanTemplateId() {
        PlantDTO dto = new PlantDTO();
        dto.setName("绿萝");
        dto.setVariety("黄金葛");
        dto.setLocation("A区");
        dto.setCarePlanTemplateId(12L);

        doAnswer(invocation -> {
            Plant plant = invocation.getArgument(0);
            plant.setId(5L);
            return 1;
        }).when(plantMapper).insert(any(Plant.class));

        plantService.createPlant(dto);

        ArgumentCaptor<Plant> captor = ArgumentCaptor.forClass(Plant.class);
        verify(plantMapper).insert(captor.capture());
        assertEquals(12L, captor.getValue().getCarePlanTemplateId());
    }

    @Test
    void updatePlantShouldRefreshCarePlanTemplateId() {
        Plant existing = new Plant();
        existing.setId(5L);
        existing.setName("旧绿萝");
        existing.setCarePlanTemplateId(3L);

        when(plantMapper.selectById(5L)).thenReturn(existing);

        PlantDTO dto = new PlantDTO();
        dto.setName("新绿萝");
        dto.setVariety("黄金葛");
        dto.setLocation("B区");
        dto.setCarePlanTemplateId(18L);

        plantService.updatePlant(5L, dto);

        ArgumentCaptor<Plant> captor = ArgumentCaptor.forClass(Plant.class);
        verify(plantMapper).updateById(captor.capture());
        assertEquals(18L, captor.getValue().getCarePlanTemplateId());
    }
}
