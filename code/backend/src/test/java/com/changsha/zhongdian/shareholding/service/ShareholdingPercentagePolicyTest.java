package com.changsha.zhongdian.shareholding.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.repository.ShareholdingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ShareholdingPercentagePolicyTest {

    @Test
    void currentActiveSumShouldOnlyCountActiveRelations() {
        PolicyFixture fixture = new PolicyFixture();
        when(fixture.shareholdingRepository
                .sumOwnershipPercentageByStationIdAndHoldingStatus(2L, HoldingStatus.ACTIVE))
                .thenReturn(new BigDecimal("90.00"));

        BigDecimal sum = fixture.policy.currentActiveSum(2L);

        // 合计口径只看“持股中”关系，已退出行不计入，与站点详情股东分区展示口径一致
        assertEquals(0, new BigDecimal("90.00").compareTo(sum));
        verify(fixture.shareholdingRepository).sumOwnershipPercentageByStationIdAndHoldingStatus(2L, HoldingStatus.ACTIVE);
    }

    @Test
    void requireSumEqualsHundredShouldAcceptHundred() {
        PolicyFixture fixture = new PolicyFixture();

        assertDoesNotThrow(() -> fixture.policy.requireSumEqualsHundred(new BigDecimal("100")));
    }

    @Test
    void requireSumEqualsHundredShouldAcceptFractionalHundred() {
        PolicyFixture fixture = new PolicyFixture();

        // 33.33 + 33.33 + 33.34 = 100.00：DECIMAL(5,2) 会回带小数位，等值判断必须用 compareTo 而非 equals
        assertDoesNotThrow(() -> fixture.policy.requireSumEqualsHundred(new BigDecimal("100.00")));
    }

    @Test
    void requireSumEqualsHundredShouldRejectBelowHundredWithGapInMessage() {
        PolicyFixture fixture = new PolicyFixture();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.policy.requireSumEqualsHundred(new BigDecimal("90"))
        );

        assertEquals("SHAREHOLDING_PERCENTAGE_SUM_MISMATCH", exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        // 错误信息必须带出当前合计与差额，调用方才能给出可操作的提示
        assertTrue(exception.getMessage().contains("当前合计 90%"), "实际信息：" + exception.getMessage());
        assertTrue(exception.getMessage().contains("还差 10%"), "实际信息：" + exception.getMessage());
    }

    @Test
    void requireSumEqualsHundredShouldRejectAboveHundredWithGapInMessage() {
        PolicyFixture fixture = new PolicyFixture();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.policy.requireSumEqualsHundred(new BigDecimal("110"))
        );

        assertEquals("SHAREHOLDING_PERCENTAGE_SUM_MISMATCH", exception.getCode());
        // 超出方向与不足方向的差额表述需区分，避免误导操作
        assertTrue(exception.getMessage().contains("超出 10%"), "实际信息：" + exception.getMessage());
    }

    @Test
    void requireSumEqualsHundredShouldRejectZeroWhenNoActiveRelations() {
        PolicyFixture fixture = new PolicyFixture();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.policy.requireSumEqualsHundred(BigDecimal.ZERO)
        );

        // 无持股中关系（合计 0）同样不满足最终态，强校验不因空数据放行
        assertEquals("SHAREHOLDING_PERCENTAGE_SUM_MISMATCH", exception.getCode());
    }

    private static class PolicyFixture {
        private final ShareholdingRepository shareholdingRepository = mock(ShareholdingRepository.class);
        private final ShareholdingPercentagePolicy policy = new ShareholdingPercentagePolicy(shareholdingRepository);
    }
}