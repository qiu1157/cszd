package com.changsha.zhongdian.pile.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.entity.SystemDictType;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.pile.dto.PileCreateRequest;
import com.changsha.zhongdian.pile.dto.PileStatusUpdateRequest;
import com.changsha.zhongdian.pile.dto.PileUpdateRequest;
import com.changsha.zhongdian.pile.entity.ChargeType;
import com.changsha.zhongdian.pile.entity.Pile;
import com.changsha.zhongdian.pile.entity.PileStatusLog;
import com.changsha.zhongdian.pile.repository.PileRepository;
import com.changsha.zhongdian.pile.repository.PileStatusLogRepository;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PileCommandServiceTest {

    private final PileRepository pileRepository = org.mockito.Mockito.mock(PileRepository.class);
    private final PileCodeGenerator pileCodeGenerator = org.mockito.Mockito.mock(PileCodeGenerator.class);
    private final StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
    private final SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
    private final PileStatusLogRepository pileStatusLogRepository = org.mockito.Mockito.mock(PileStatusLogRepository.class);

    private final PileCommandService pileCommandService = new PileCommandService(
            pileRepository,
            pileCodeGenerator,
            stationRepository,
            dictItemRepository,
            pileStatusLogRepository
    );

    @Test
    void createPileShouldSaveWithGeneratedCodeAndDefaultStatus() {
        when(stationRepository.existsById(1001L)).thenReturn(true);
        when(dictItemRepository.findByTypeTypeCodeAndItemNameAndItemStatus(
                DictionaryTypeCode.PILE_STATUS, "运营中", DictionaryStatus.ENABLED
        )).thenReturn(Optional.of(createDictItem(31L, DictionaryTypeCode.PILE_STATUS, "运营中")));
        when(pileCodeGenerator.generateNextCode()).thenReturn("PL-202609-0001");
        when(pileRepository.save(any(Pile.class))).thenAnswer(invocation -> {
            Pile pile = invocation.getArgument(0);
            java.lang.reflect.Field idField = Pile.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pile, 21L);
            return pile;
        });

        Pile result = pileCommandService.createPile(1001L, new PileCreateRequest("FAST", 2));

        assertEquals(21L, result.getId());
        ArgumentCaptor<Pile> pileCaptor = ArgumentCaptor.forClass(Pile.class);
        verify(pileRepository).save(pileCaptor.capture());
        Pile savedPile = pileCaptor.getValue();
        assertEquals("PL-202609-0001", savedPile.getPileCode());
        assertEquals(1001L, savedPile.getStationId());
        assertEquals(ChargeType.FAST, savedPile.getChargeType());
        assertEquals(2, savedPile.getGunCount());
        assertEquals(31L, savedPile.getPileStatusItemId());
    }

    @Test
    void createPileShouldRejectInvalidStationId() {
        BusinessException nullIdException = assertThrows(
                BusinessException.class,
                () -> pileCommandService.createPile(null, new PileCreateRequest("FAST", 2))
        );
        assertEquals("VALIDATION_ERROR", nullIdException.getCode());

        BusinessException zeroIdException = assertThrows(
                BusinessException.class,
                () -> pileCommandService.createPile(0L, new PileCreateRequest("FAST", 2))
        );
        assertEquals("VALIDATION_ERROR", zeroIdException.getCode());
    }

    @Test
    void createPileShouldRejectWhenStationMissing() {
        when(stationRepository.existsById(999L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pileCommandService.createPile(999L, new PileCreateRequest("FAST", 2))
        );

        assertEquals("STATION_NOT_FOUND", exception.getCode());
    }

    @Test
    void createPileShouldRejectInvalidChargeType() {
        when(stationRepository.existsById(1001L)).thenReturn(true);

        BusinessException nullTypeException = assertThrows(
                BusinessException.class,
                () -> pileCommandService.createPile(1001L, new PileCreateRequest(null, 2))
        );
        assertEquals("VALIDATION_ERROR", nullTypeException.getCode());

        BusinessException blankTypeException = assertThrows(
                BusinessException.class,
                () -> pileCommandService.createPile(1001L, new PileCreateRequest("  ", 2))
        );
        assertEquals("VALIDATION_ERROR", blankTypeException.getCode());

        BusinessException unknownTypeException = assertThrows(
                BusinessException.class,
                () -> pileCommandService.createPile(1001L, new PileCreateRequest("QUICK", 2))
        );
        assertEquals("VALIDATION_ERROR", unknownTypeException.getCode());
    }

    @Test
    void createPileShouldRejectInvalidGunCount() {
        when(stationRepository.existsById(1001L)).thenReturn(true);

        BusinessException nullCountException = assertThrows(
                BusinessException.class,
                () -> pileCommandService.createPile(1001L, new PileCreateRequest("FAST", null))
        );
        assertEquals("VALIDATION_ERROR", nullCountException.getCode());

        BusinessException zeroCountException = assertThrows(
                BusinessException.class,
                () -> pileCommandService.createPile(1001L, new PileCreateRequest("FAST", 0))
        );
        assertEquals("VALIDATION_ERROR", zeroCountException.getCode());

        BusinessException overLimitException = assertThrows(
                BusinessException.class,
                () -> pileCommandService.createPile(1001L, new PileCreateRequest("FAST", 65))
        );
        assertEquals("VALIDATION_ERROR", overLimitException.getCode());
    }

    @Test
    void createPileShouldRejectWhenDefaultStatusMissing() {
        when(stationRepository.existsById(1001L)).thenReturn(true);
        when(dictItemRepository.findByTypeTypeCodeAndItemNameAndItemStatus(
                DictionaryTypeCode.PILE_STATUS, "运营中", DictionaryStatus.ENABLED
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pileCommandService.createPile(1001L, new PileCreateRequest("FAST", 2))
        );

        assertEquals("PILE_STATUS_NOT_FOUND", exception.getCode());
    }

    @Test
    void updatePileShouldUpdateChargeTypeAndGunCountWithoutTouchingStatus() throws ReflectiveOperationException {
        Pile existingPile = new Pile();
        // 为什么用反射：JPA 实体主键由数据库生成无 setter，测试按 findById 的真实语义补齐主键
        java.lang.reflect.Field idField = Pile.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(existingPile, 21L);
        existingPile.setPileCode("PL-202609-0001");
        existingPile.setStationId(1001L);
        existingPile.setChargeType(ChargeType.FAST);
        existingPile.setGunCount(2);
        existingPile.setPileStatusItemId(31L);
        when(pileRepository.findById(21L)).thenReturn(Optional.of(existingPile));
        when(pileRepository.save(any(Pile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pile result = pileCommandService.updatePile(1001L, 21L, new PileUpdateRequest("SLOW", 1));

        assertEquals(21L, result.getId());
        ArgumentCaptor<Pile> pileCaptor = ArgumentCaptor.forClass(Pile.class);
        verify(pileRepository).save(pileCaptor.capture());
        Pile savedPile = pileCaptor.getValue();
        assertEquals("PL-202609-0001", savedPile.getPileCode());
        assertEquals(ChargeType.SLOW, savedPile.getChargeType());
        assertEquals(1, savedPile.getGunCount());
        // 为什么断言状态字典项不变：编辑与状态变更必须分离，状态只能通过状态切换接口修改
        assertEquals(31L, savedPile.getPileStatusItemId());
    }

    @Test
    void updatePileShouldRejectInvalidStationId() {
        BusinessException nullIdException = assertThrows(
                BusinessException.class,
                () -> pileCommandService.updatePile(null, 21L, new PileUpdateRequest("FAST", 2))
        );
        assertEquals("VALIDATION_ERROR", nullIdException.getCode());
    }

    @Test
    void updatePileShouldRejectWhenPileMissing() {
        when(pileRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pileCommandService.updatePile(1001L, 99L, new PileUpdateRequest("FAST", 2))
        );

        assertEquals("PILE_NOT_FOUND", exception.getCode());
    }

    @Test
    void updatePileShouldRejectWhenPileBelongsToOtherStation() {
        Pile otherStationPile = new Pile();
        otherStationPile.setPileCode("PL-202609-0009");
        otherStationPile.setStationId(2002L);
        otherStationPile.setChargeType(ChargeType.FAST);
        otherStationPile.setGunCount(2);
        otherStationPile.setPileStatusItemId(31L);
        when(pileRepository.findById(21L)).thenReturn(Optional.of(otherStationPile));

        // 为什么返回 404 而非 403：不向调用方暴露“该桩存在于其他站点”的信息，与资源归属校验口径一致
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pileCommandService.updatePile(1001L, 21L, new PileUpdateRequest("FAST", 2))
        );

        assertEquals("PILE_NOT_FOUND", exception.getCode());
    }

    @Test
    void updatePileStatusShouldSwitchStatusAndWriteLog() throws ReflectiveOperationException {
        Pile existingPile = newPileWithId(21L, 1001L, 31L);
        when(pileRepository.findById(21L)).thenReturn(Optional.of(existingPile));
        when(pileRepository.save(any(Pile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dictItemRepository.findByIdAndTypeTypeCodeAndItemStatus(
                33L, DictionaryTypeCode.PILE_STATUS, DictionaryStatus.ENABLED
        )).thenReturn(Optional.of(createDictItem(33L, DictionaryTypeCode.PILE_STATUS, "停用")));

        Pile result = pileCommandService.updatePileStatus(
                1001L, 21L, new PileStatusUpdateRequest(33L, "设备检修停用"), "13800000001"
        );

        assertEquals(21L, result.getId());
        ArgumentCaptor<Pile> pileCaptor = ArgumentCaptor.forClass(Pile.class);
        verify(pileRepository).save(pileCaptor.capture());
        assertEquals(33L, pileCaptor.getValue().getPileStatusItemId());

        // 为什么单独捕获日志：状态切换的核心诉求是留痕，必须断言原状态/新状态/备注/操作人完整落库
        ArgumentCaptor<PileStatusLog> logCaptor = ArgumentCaptor.forClass(PileStatusLog.class);
        verify(pileStatusLogRepository).save(logCaptor.capture());
        PileStatusLog savedLog = logCaptor.getValue();
        assertEquals(21L, savedLog.getPileId());
        assertEquals(31L, savedLog.getFromStatusItemId());
        assertEquals(33L, savedLog.getToStatusItemId());
        assertEquals("设备检修停用", savedLog.getRemark());
        assertEquals("13800000001", savedLog.getOperatorAccount());
    }

    @Test
    void updatePileStatusShouldTrimBlankRemarkToNull() throws ReflectiveOperationException {
        when(pileRepository.findById(21L)).thenReturn(Optional.of(newPileWithId(21L, 1001L, 31L)));
        when(pileRepository.save(any(Pile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dictItemRepository.findByIdAndTypeTypeCodeAndItemStatus(
                32L, DictionaryTypeCode.PILE_STATUS, DictionaryStatus.ENABLED
        )).thenReturn(Optional.of(createDictItem(32L, DictionaryTypeCode.PILE_STATUS, "运营异常")));

        pileCommandService.updatePileStatus(1001L, 21L, new PileStatusUpdateRequest(32L, "   "), "13800000001");

        ArgumentCaptor<PileStatusLog> logCaptor = ArgumentCaptor.forClass(PileStatusLog.class);
        verify(pileStatusLogRepository).save(logCaptor.capture());
        assertNull(logCaptor.getValue().getRemark());
    }

    @Test
    void updatePileStatusShouldRejectInvalidStationId() {
        BusinessException nullIdException = assertThrows(
                BusinessException.class,
                () -> pileCommandService.updatePileStatus(null, 21L, new PileStatusUpdateRequest(33L, null), "13800000001")
        );
        assertEquals("VALIDATION_ERROR", nullIdException.getCode());

        BusinessException zeroIdException = assertThrows(
                BusinessException.class,
                () -> pileCommandService.updatePileStatus(0L, 21L, new PileStatusUpdateRequest(33L, null), "13800000001")
        );
        assertEquals("VALIDATION_ERROR", zeroIdException.getCode());
    }

    @Test
    void updatePileStatusShouldRejectWhenPileMissing() {
        when(pileRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pileCommandService.updatePileStatus(1001L, 99L, new PileStatusUpdateRequest(33L, null), "13800000001")
        );

        assertEquals("PILE_NOT_FOUND", exception.getCode());
    }

    @Test
    void updatePileStatusShouldRejectWhenPileBelongsToOtherStation() throws ReflectiveOperationException {
        when(pileRepository.findById(21L)).thenReturn(Optional.of(newPileWithId(21L, 2002L, 31L)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pileCommandService.updatePileStatus(1001L, 21L, new PileStatusUpdateRequest(33L, null), "13800000001")
        );

        assertEquals("PILE_NOT_FOUND", exception.getCode());
    }

    @Test
    void updatePileStatusShouldRejectInvalidStatusItemId() throws ReflectiveOperationException {
        when(pileRepository.findById(21L)).thenReturn(Optional.of(newPileWithId(21L, 1001L, 31L)));

        BusinessException nullStatusException = assertThrows(
                BusinessException.class,
                () -> pileCommandService.updatePileStatus(1001L, 21L, new PileStatusUpdateRequest(null, null), "13800000001")
        );
        assertEquals("VALIDATION_ERROR", nullStatusException.getCode());

        BusinessException zeroStatusException = assertThrows(
                BusinessException.class,
                () -> pileCommandService.updatePileStatus(1001L, 21L, new PileStatusUpdateRequest(0L, null), "13800000001")
        );
        assertEquals("VALIDATION_ERROR", zeroStatusException.getCode());
    }

    @Test
    void updatePileStatusShouldRejectWhenTargetStatusMissingOrDisabled() throws ReflectiveOperationException {
        when(pileRepository.findById(21L)).thenReturn(Optional.of(newPileWithId(21L, 1001L, 31L)));
        when(dictItemRepository.findByIdAndTypeTypeCodeAndItemStatus(
                99L, DictionaryTypeCode.PILE_STATUS, DictionaryStatus.ENABLED
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pileCommandService.updatePileStatus(1001L, 21L, new PileStatusUpdateRequest(99L, null), "13800000001")
        );

        assertEquals("PILE_STATUS_NOT_FOUND", exception.getCode());
    }

    private Pile newPileWithId(Long id, Long stationId, Long statusItemId) throws ReflectiveOperationException {
        Pile pile = new Pile();
        // 为什么用反射：JPA 实体主键由数据库生成无 setter，测试按 findById 的真实语义补齐主键
        java.lang.reflect.Field idField = Pile.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(pile, id);
        pile.setPileCode("PL-202609-0001");
        pile.setStationId(stationId);
        pile.setChargeType(ChargeType.FAST);
        pile.setGunCount(2);
        pile.setPileStatusItemId(statusItemId);
        return pile;
    }

    private SystemDictItem createDictItem(Long id, DictionaryTypeCode typeCode, String itemName) {
        SystemDictType type = new SystemDictType();
        type.setTypeCode(typeCode);
        type.setTypeName(typeCode.name());
        type.setTypeStatus(DictionaryStatus.ENABLED);
        type.setSortOrder(10);

        SystemDictItem item = new SystemDictItem();
        item.setType(type);
        item.setItemCode(itemName);
        item.setItemName(itemName);
        item.setItemStatus(DictionaryStatus.ENABLED);
        item.setSortOrder(10);
        try {
            java.lang.reflect.Field idField = SystemDictItem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(item, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("测试数据构造失败", exception);
        }
        return item;
    }
}
