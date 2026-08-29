package com.changsha.zhongdian.station.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import com.changsha.zhongdian.station.entity.Station;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.junit.jupiter.api.Test;

class StationCodeGeneratorTest {

    @Test
    void generateNextCodeShouldStartFrom0001WhenCurrentMonthHasNoStation() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-29T08:00:00Z"), ZoneId.of("Asia/Shanghai"));
        when(stationRepository.findTopByStationCodeStartingWithOrderByStationCodeDesc("ST-202608-"))
                .thenReturn(Optional.empty());

        StationCodeGenerator stationCodeGenerator = new StationCodeGenerator(stationRepository, clock);

        String stationCode = stationCodeGenerator.generateNextCode();

        assertEquals("ST-202608-0001", stationCode);
        verify(stationRepository).findTopByStationCodeStartingWithOrderByStationCodeDesc("ST-202608-");
    }

    @Test
    void generateNextCodeShouldIncrementSequenceWithinCurrentMonth() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-29T08:00:00Z"), ZoneId.of("Asia/Shanghai"));
        Station existingStation = new Station();
        existingStation.setStationCode("ST-202608-0018");
        when(stationRepository.findTopByStationCodeStartingWithOrderByStationCodeDesc("ST-202608-"))
                .thenReturn(Optional.of(existingStation));

        StationCodeGenerator stationCodeGenerator = new StationCodeGenerator(stationRepository, clock);

        String stationCode = stationCodeGenerator.generateNextCode();

        assertEquals("ST-202608-0019", stationCode);
    }

    @Test
    void generateNextCodeShouldRestartSequenceWhenMonthChanges() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-09-03T08:00:00Z"), ZoneId.of("Asia/Shanghai"));
        when(stationRepository.findTopByStationCodeStartingWithOrderByStationCodeDesc("ST-202609-"))
                .thenReturn(Optional.empty());

        StationCodeGenerator stationCodeGenerator = new StationCodeGenerator(stationRepository, clock);

        String stationCode = stationCodeGenerator.generateNextCode();

        assertEquals("ST-202609-0001", stationCode);
    }

    @Test
    void generateNextCodeShouldRejectMalformedExistingCode() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-29T08:00:00Z"), ZoneId.of("Asia/Shanghai"));
        Station existingStation = new Station();
        existingStation.setStationCode("ST-202608-ABCD");
        when(stationRepository.findTopByStationCodeStartingWithOrderByStationCodeDesc("ST-202608-"))
                .thenReturn(Optional.of(existingStation));

        StationCodeGenerator stationCodeGenerator = new StationCodeGenerator(stationRepository, clock);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                stationCodeGenerator::generateNextCode
        );

        assertEquals("现有充电站编号格式不正确，无法继续生成新编号", exception.getMessage());
    }
}
