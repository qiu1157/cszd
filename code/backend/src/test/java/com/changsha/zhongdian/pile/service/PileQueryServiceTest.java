package com.changsha.zhongdian.pile.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.pile.dto.PileListItemResponse;
import com.changsha.zhongdian.pile.repository.PileListItemProjection;
import com.changsha.zhongdian.pile.repository.PileRepository;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.junit.jupiter.api.Test;

class PileQueryServiceTest {

    private final PileRepository pileRepository = org.mockito.Mockito.mock(PileRepository.class);
    private final StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
    private final PileCodeGenerator pileCodeGenerator = org.mockito.Mockito.mock(PileCodeGenerator.class);
    private final com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository.class);
    private final PileQueryService pileQueryService = new PileQueryService(
            pileRepository,
            stationRepository,
            pileCodeGenerator,
            dictItemRepository
    );

    @Test
    void nextPileCodeShouldRequireExistingStation() {
        when(stationRepository.existsById(999L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pileQueryService.nextPileCode(999L)
        );

        assertEquals("STATION_NOT_FOUND", exception.getCode());
    }

    @Test
    void nextPileCodeShouldReturnGeneratedCodeForExistingStation() {
        when(stationRepository.existsById(1001L)).thenReturn(true);
        when(pileCodeGenerator.generateNextCode()).thenReturn("PL-202609-0001");

        String pileCode = pileQueryService.nextPileCode(1001L);

        assertEquals("PL-202609-0001", pileCode);
    }

    @Test
    void listPilesShouldRejectInvalidStationId() {
        BusinessException nullIdException = assertThrows(
                BusinessException.class,
                () -> pileQueryService.listPilesByStationId(null)
        );
        assertEquals("VALIDATION_ERROR", nullIdException.getCode());

        BusinessException negativeIdException = assertThrows(
                BusinessException.class,
                () -> pileQueryService.listPilesByStationId(0L)
        );
        assertEquals("VALIDATION_ERROR", negativeIdException.getCode());
    }

    @Test
    void listPilesShouldRejectWhenStationDoesNotExist() {
        when(stationRepository.existsById(999L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pileQueryService.listPilesByStationId(999L)
        );

        assertEquals("STATION_NOT_FOUND", exception.getCode());
    }

    @Test
    void listPilesShouldReturnPileRowsWithReadableChargeType() {
        when(stationRepository.existsById(1001L)).thenReturn(true);
        when(pileRepository.findAllListItemsByStationIdOrderByUpdatedAtDesc(1001L)).thenReturn(List.of(
                new TestPileListItemProjection(21L, "PL-202609-0001", "FAST", 2, "运营中",
                        LocalDateTime.of(2026, 9, 1, 10, 0)),
                new TestPileListItemProjection(22L, "PL-202609-0002", "SLOW", 1, "运营异常",
                        LocalDateTime.of(2026, 9, 1, 11, 0))
        ));

        List<PileListItemResponse> piles = pileQueryService.listPilesByStationId(1001L);

        assertEquals(2, piles.size());
        assertEquals("PL-202609-0001", piles.get(0).pileCode());
        assertEquals("快充", piles.get(0).chargeTypeName());
        assertEquals(2, piles.get(0).gunCount());
        assertEquals("运营中", piles.get(0).pileStatusName());
        assertEquals("慢充", piles.get(1).chargeTypeName());
    }

    private static final class TestPileListItemProjection implements PileListItemProjection {

        private final Long id;
        private final String pileCode;
        private final String chargeType;
        private final Integer gunCount;
        private final String pileStatusName;
        private final LocalDateTime updatedAt;

        private TestPileListItemProjection(
                Long id,
                String pileCode,
                String chargeType,
                Integer gunCount,
                String pileStatusName,
                LocalDateTime updatedAt
        ) {
            this.id = id;
            this.pileCode = pileCode;
            this.chargeType = chargeType;
            this.gunCount = gunCount;
            this.pileStatusName = pileStatusName;
            this.updatedAt = updatedAt;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getPileCode() {
            return pileCode;
        }

        @Override
        public String getChargeType() {
            return chargeType;
        }

        @Override
        public Integer getGunCount() {
            return gunCount;
        }

        @Override
        public String getPileStatusName() {
            return pileStatusName;
        }

        @Override
        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }
    }
}
