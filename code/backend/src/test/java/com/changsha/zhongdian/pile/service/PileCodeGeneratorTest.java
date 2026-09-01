package com.changsha.zhongdian.pile.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import com.changsha.zhongdian.pile.entity.Pile;
import com.changsha.zhongdian.pile.repository.PileRepository;
import org.junit.jupiter.api.Test;

class PileCodeGeneratorTest {

    @Test
    void generateNextCodeShouldStartFrom0001WhenCurrentMonthHasNoPile() {
        PileRepository pileRepository = org.mockito.Mockito.mock(PileRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-09-01T08:00:00Z"), ZoneId.of("Asia/Shanghai"));
        when(pileRepository.findTopByPileCodeStartingWithOrderByPileCodeDesc("PL-202609-"))
                .thenReturn(Optional.empty());

        PileCodeGenerator pileCodeGenerator = new PileCodeGenerator(pileRepository, clock);

        String pileCode = pileCodeGenerator.generateNextCode();

        assertEquals("PL-202609-0001", pileCode);
        verify(pileRepository).findTopByPileCodeStartingWithOrderByPileCodeDesc("PL-202609-");
    }

    @Test
    void generateNextCodeShouldIncrementSequenceWithinCurrentMonth() {
        PileRepository pileRepository = org.mockito.Mockito.mock(PileRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-09-01T08:00:00Z"), ZoneId.of("Asia/Shanghai"));
        Pile existingPile = new Pile();
        existingPile.setPileCode("PL-202609-2042");
        when(pileRepository.findTopByPileCodeStartingWithOrderByPileCodeDesc("PL-202609-"))
                .thenReturn(Optional.of(existingPile));

        PileCodeGenerator pileCodeGenerator = new PileCodeGenerator(pileRepository, clock);

        String pileCode = pileCodeGenerator.generateNextCode();

        assertEquals("PL-202609-2043", pileCode);
    }

    @Test
    void generateNextCodeShouldRestartSequenceWhenMonthChanges() {
        PileRepository pileRepository = org.mockito.Mockito.mock(PileRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-10-01T08:00:00Z"), ZoneId.of("Asia/Shanghai"));
        when(pileRepository.findTopByPileCodeStartingWithOrderByPileCodeDesc("PL-202610-"))
                .thenReturn(Optional.empty());

        PileCodeGenerator pileCodeGenerator = new PileCodeGenerator(pileRepository, clock);

        String pileCode = pileCodeGenerator.generateNextCode();

        assertEquals("PL-202610-0001", pileCode);
    }

    @Test
    void generateNextCodeShouldRejectMalformedExistingCode() {
        PileRepository pileRepository = org.mockito.Mockito.mock(PileRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-09-01T08:00:00Z"), ZoneId.of("Asia/Shanghai"));
        Pile existingPile = new Pile();
        existingPile.setPileCode("PL-202609-ABCD");
        when(pileRepository.findTopByPileCodeStartingWithOrderByPileCodeDesc("PL-202609-"))
                .thenReturn(Optional.of(existingPile));

        PileCodeGenerator pileCodeGenerator = new PileCodeGenerator(pileRepository, clock);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                pileCodeGenerator::generateNextCode
        );

        assertEquals("现有充电桩编号格式不正确，无法继续生成新编号", exception.getMessage());
    }
}
