package com.changsha.zhongdian.shareholder.service;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.shareholder.dto.ShareholderCreateRequest;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShareholderCommandService {

    private final ShareholderRepository shareholderRepository;

    public ShareholderCommandService(ShareholderRepository shareholderRepository) {
        this.shareholderRepository = shareholderRepository;
    }

    @Transactional
    public Long createShareholder(ShareholderCreateRequest request) {
        String shareholderName = requireNonBlank(request.shareholderName(), "请输入股东名称");
        ShareholderStatus shareholderStatus = resolveShareholderStatus(request.shareholderStatus());

        Shareholder shareholder = new Shareholder();
        shareholder.setShareholderName(shareholderName);
        shareholder.setContactName(trimToNull(request.contactName()));
        shareholder.setContactPhone(trimToNull(request.contactPhone()));
        shareholder.setShareholderStatus(shareholderStatus);

        return shareholderRepository.save(shareholder).getId();
    }

    // 为什么显式解析：@NotBlank 只能兜住空值，非法枚举字符串若放任不拦截会被静默处理或抛底层异常，必须在业务层给出明确提示
    private ShareholderStatus resolveShareholderStatus(String shareholderStatus) {
        if (shareholderStatus == null || shareholderStatus.trim().isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "请选择股东状态", HttpStatus.BAD_REQUEST);
        }

        try {
            return ShareholderStatus.valueOf(shareholderStatus.trim());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("SHAREHOLDER_STATUS_INVALID", "股东状态不合法", HttpStatus.BAD_REQUEST);
        }
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalizedValue = value.trim();
        return normalizedValue.isEmpty() ? null : normalizedValue;
    }
}