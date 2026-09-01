package com.changsha.zhongdian.pile.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.changsha.zhongdian.pile.entity.Pile;
import com.changsha.zhongdian.pile.repository.PileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PileCodeGenerator {

    private static final String CODE_PREFIX = "PL-";
    private static final int SEQUENCE_WIDTH = 4;
    private static final int MAX_SEQUENCE = 9_999;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final PileRepository pileRepository;
    private final Clock clock;

    @Autowired
    public PileCodeGenerator(PileRepository pileRepository) {
        this(pileRepository, Clock.systemDefaultZone());
    }

    PileCodeGenerator(PileRepository pileRepository, Clock clock) {
        this.pileRepository = pileRepository;
        this.clock = clock;
    }

    public String generateNextCode() {
        String codePrefix = CODE_PREFIX + LocalDate.now(clock).format(MONTH_FORMATTER) + "-";
        Optional<Pile> latestPile = pileRepository.findTopByPileCodeStartingWithOrderByPileCodeDesc(codePrefix);

        if (latestPile.isEmpty()) {
            return codePrefix + formatSequence(1);
        }

        String latestCode = latestPile.get().getPileCode();
        if (latestCode == null || latestCode.length() <= codePrefix.length()) {
            throw new IllegalStateException("现有充电桩编号格式不正确，无法继续生成新编号");
        }
        String latestSequence = latestCode.substring(codePrefix.length());
        if (!latestSequence.matches("\\d{4}")) {
            throw new IllegalStateException("现有充电桩编号格式不正确，无法继续生成新编号");
        }

        int nextSequence = Integer.parseInt(latestSequence) + 1;
        if (nextSequence > MAX_SEQUENCE) {
            throw new IllegalStateException("当前月份的充电桩编号已用尽，无法继续生成新编号");
        }

        return codePrefix + formatSequence(nextSequence);
    }

    private String formatSequence(int sequence) {
        return String.format("%0" + SEQUENCE_WIDTH + "d", sequence);
    }
}
