package com.plantadoption.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.common.ErrorCode;
import com.plantadoption.dto.DriftBottleReplyDTO;
import com.plantadoption.dto.DriftBottleThrowDTO;
import com.plantadoption.entity.DriftBottle;
import com.plantadoption.exception.BusinessException;
import com.plantadoption.mapper.DriftBottleMapper;
import com.plantadoption.mapper.DriftBottleLogMapper;
import com.plantadoption.service.NotificationService;
import com.plantadoption.service.SysConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriftBottleServiceImplTest {

    @Mock
    private DriftBottleMapper driftBottleMapper;

    @Mock
    private DriftBottleLogMapper driftBottleLogMapper;

    @Mock
    private SysConfigService sysConfigService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private DriftBottleServiceImpl driftBottleService;

    @Test
    void throwBottleShouldRejectBlankContent() {
        DriftBottleThrowDTO dto = new DriftBottleThrowDTO();
        dto.setContent("   ");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> driftBottleService.throwBottle(9L, dto));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void throwBottleShouldPersistFloatingBottleWhenWithinDailyLimit() {
        DriftBottleThrowDTO dto = new DriftBottleThrowDTO();
        dto.setContent("今天的绿植养护好有成就感");
        dto.setImageUrls(List.of("https://example.com/a.png"));

        when(sysConfigService.getIntConfigValue("drift_bottle_daily_limit", 3)).thenReturn(3);
        when(driftBottleMapper.countDailySentBySender(9L)).thenReturn(1);
        doAnswer(invocation -> {
            DriftBottle bottle = invocation.getArgument(0);
            bottle.setId(88L);
            return 1;
        }).when(driftBottleMapper).insert(any(DriftBottle.class));

        DriftBottle bottle = driftBottleService.throwBottle(9L, dto);

        assertEquals(88L, bottle.getId());
        assertEquals(9L, bottle.getSenderId());
        assertEquals("FLOATING", bottle.getStatus());
        assertTrue(bottle.getContent().contains("成就感"));
    }

    @Test
    void pickBottleShouldAssignReceiverAndUpdateStatus() {
        DriftBottle bottle = new DriftBottle();
        bottle.setId(12L);
        bottle.setSenderId(3L);
        bottle.setStatus("FLOATING");
        when(driftBottleMapper.selectOne(any())).thenReturn(bottle);

        DriftBottle picked = driftBottleService.pickBottle(9L);

        assertEquals(9L, picked.getReceiverId());
        assertEquals("PICKED", picked.getStatus());
        verify(driftBottleMapper).updateById(bottle);
    }

    @Test
    void replyBottleShouldRejectNonReceiver() {
        DriftBottle bottle = new DriftBottle();
        bottle.setId(15L);
        bottle.setSenderId(3L);
        bottle.setReceiverId(8L);
        bottle.setStatus("PICKED");
        when(driftBottleMapper.selectById(15L)).thenReturn(bottle);

        DriftBottleReplyDTO dto = new DriftBottleReplyDTO();
        dto.setReplyContent("收到啦");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> driftBottleService.replyBottle(9L, 15L, dto));

        assertEquals(ErrorCode.FORBIDDEN.getCode(), exception.getCode());
    }

    @Test
    void pageBottlesShouldFilterAndReturnPage() {
        Page<DriftBottle> page = new Page<>(1, 10);
        DriftBottle bottle = new DriftBottle();
        bottle.setId(21L);
        Page<DriftBottle> result = new Page<>(1, 10);
        result.setRecords(List.of(bottle));
        result.setTotal(1);

        when(driftBottleMapper.selectPage(any(Page.class), any())).thenReturn(result);

        Page<DriftBottle> actual = (Page<DriftBottle>) driftBottleService.pageBottles(page, "FLOATING", "绿植");

        assertEquals(1, actual.getTotal());
        assertEquals(21L, actual.getRecords().get(0).getId());
    }

    @Test
    void deleteBottleShouldRejectMissingBottle() {
        when(driftBottleMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> driftBottleService.deleteBottle(99L));

        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void replyBottleShouldNotifySenderAfterSuccess() {
        DriftBottle bottle = new DriftBottle();
        bottle.setId(15L);
        bottle.setSenderId(3L);
        bottle.setReceiverId(8L);
        bottle.setStatus("PICKED");
        when(driftBottleMapper.selectById(15L)).thenReturn(bottle);

        DriftBottleReplyDTO dto = new DriftBottleReplyDTO();
        dto.setReplyContent("鏀跺埌浜嗭紝绁濅綘寮€蹇?");

        DriftBottle replied = driftBottleService.replyBottle(8L, 15L, dto);

        assertEquals("REPLIED", replied.getStatus());
        verify(notificationService).sendNotification(3L, "漂流瓶收到回复", "您投递的漂流瓶已收到新的回复，快去看看吧。", "SYSTEM", 15L, "DriftBottle");
    }
}
