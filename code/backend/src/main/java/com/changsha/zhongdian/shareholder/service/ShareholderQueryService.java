package com.changsha.zhongdian.shareholder.service;

import java.util.List;

import com.changsha.zhongdian.shareholder.dto.ShareholderListItemResponse;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import org.springframework.stereotype.Service;

@Service
public class ShareholderQueryService {

    private final ShareholderRepository shareholderRepository;

    public ShareholderQueryService(ShareholderRepository shareholderRepository) {
        this.shareholderRepository = shareholderRepository;
    }

    public List<ShareholderListItemResponse> listShareholders(String shareholderNameKeyword, String shareholderStatusValue) {
        String normalizedKeyword = normalizeKeyword(shareholderNameKeyword);
        ShareholderStatus normalizedStatus = normalizeStatus(shareholderStatusValue);

        return resolveShareholders(normalizedKeyword, normalizedStatus).stream()
                .map(this::toListItemResponse)
                .toList();
    }

    private List<Shareholder> resolveShareholders(String shareholderNameKeyword, ShareholderStatus shareholderStatus) {
        if (shareholderNameKeyword != null && shareholderStatus != null) {
            return shareholderRepository.findAllByShareholderNameContainingAndShareholderStatusOrderByUpdatedAtDesc(
                    shareholderNameKeyword,
                    shareholderStatus
            );
        }
        if (shareholderNameKeyword != null) {
            return shareholderRepository.findAllByShareholderNameContainingOrderByUpdatedAtDesc(shareholderNameKeyword);
        }
        if (shareholderStatus != null) {
            return shareholderRepository.findAllByShareholderStatusOrderByUpdatedAtDesc(shareholderStatus);
        }
        return shareholderRepository.findAllByOrderByUpdatedAtDesc();
    }

    private ShareholderListItemResponse toListItemResponse(Shareholder shareholder) {
        return new ShareholderListItemResponse(
                shareholder.getId(),
                shareholder.getShareholderName(),
                shareholder.getContactName(),
                shareholder.getContactPhone(),
                shareholder.getShareholderStatus().name(),
                shareholder.getUpdatedAt()
        );
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String normalizedKeyword = keyword.trim();
        return normalizedKeyword.isEmpty() ? null : normalizedKeyword;
    }

    private ShareholderStatus normalizeStatus(String shareholderStatusValue) {
        if (shareholderStatusValue == null) {
            return null;
        }
        String normalizedValue = shareholderStatusValue.trim();
        if (normalizedValue.isEmpty()) {
            return null;
        }
        try {
            return ShareholderStatus.valueOf(normalizedValue);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}