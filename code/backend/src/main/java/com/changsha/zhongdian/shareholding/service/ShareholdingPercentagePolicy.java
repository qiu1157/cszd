package com.changsha.zhongdian.shareholding.service;

import java.math.BigDecimal;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.repository.ShareholdingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * 持股占比强校验：spec 7.5 规定“=100% 是最终态强校验”，触发点是股权调整保存与退股分摊，
 * 新增持股接口不走本规则（新增仅拦截“合计将超过 100%”，允许暂低于 100% 由股权调整统一补齐）。
 */
@Component
public class ShareholdingPercentagePolicy {

    private static final BigDecimal FULL_OWNERSHIP = new BigDecimal("100");

    private final ShareholdingRepository shareholdingRepository;

    public ShareholdingPercentagePolicy(ShareholdingRepository shareholdingRepository) {
        this.shareholdingRepository = shareholdingRepository;
    }

    // 强校验接收入参合计而非查库：股权调整保存要校验“提交后的最终态”，退股分摊要校验“调整后的合计”，
    // 两者都不能读库中旧行；需要当前口径时由调用方显式走 currentActiveSum
    public void requireSumEqualsHundred(BigDecimal sum) {
        int direction = FULL_OWNERSHIP.compareTo(sum);
        if (direction == 0) {
            return;
        }
        String gap = FULL_OWNERSHIP.subtract(sum).abs().stripTrailingZeros().toPlainString();
        String directionText = direction > 0 ? "还差" : "超出";
        throw new BusinessException(
                "SHAREHOLDING_PERCENTAGE_SUM_MISMATCH",
                "持股中股东占比总和必须为 100%，当前合计 " + sum.stripTrailingZeros().toPlainString()
                        + "%，" + directionText + " " + gap + "%",
                HttpStatus.BAD_REQUEST
        );
    }

    // 合计口径只看“持股中”关系；仓储查询已 COALESCE 兜 0，调用方免判空
    public BigDecimal currentActiveSum(Long stationId) {
        return shareholdingRepository.sumOwnershipPercentageByStationIdAndHoldingStatus(stationId, HoldingStatus.ACTIVE);
    }
}