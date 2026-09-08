package com.changsha.zhongdian.shareholder.service;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.operationlog.annotation.LogOperation;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.entity.OperationType;
import com.changsha.zhongdian.shareholder.dto.ShareholderCreateRequest;
import com.changsha.zhongdian.shareholder.dto.ShareholderUpdateRequest;
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
    @LogOperation(
            targetType = OperationTargetType.SHAREHOLDER,
            operationType = OperationType.CREATE,
            targetIdSpEL = "#result.id",
            targetNameSpEL = "#result.shareholderName"
    )
    public Shareholder createShareholder(ShareholderCreateRequest request) {
        String shareholderName = requireNonBlank(request.shareholderName(), "请输入股东名称");
        ShareholderStatus shareholderStatus = resolveShareholderStatus(request.shareholderStatus());

        Shareholder shareholder = new Shareholder();
        shareholder.setShareholderName(shareholderName);
        shareholder.setContactName(trimToNull(request.contactName()));
        shareholder.setContactPhone(trimToNull(request.contactPhone()));
        shareholder.setShareholderStatus(shareholderStatus);

        return shareholderRepository.save(shareholder);
    }

    @Transactional
    @LogOperation(
            targetType = OperationTargetType.SHAREHOLDER,
            operationType = OperationType.UPDATE,
            targetIdSpEL = "#result.id",
            targetNameSpEL = "#result.shareholderName"
    )
    public Shareholder updateShareholder(Long shareholderId, ShareholderUpdateRequest request) {
        if (shareholderId == null || shareholderId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "股东不存在", HttpStatus.BAD_REQUEST);
        }

        Shareholder shareholder = shareholderRepository.findById(shareholderId)
                .orElseThrow(() -> new BusinessException("SHAREHOLDER_NOT_FOUND", "股东不存在", HttpStatus.NOT_FOUND));

        shareholder.setShareholderName(requireNonBlank(request.shareholderName(), "请输入股东名称"));
        shareholder.setContactName(trimToNull(request.contactName()));
        shareholder.setContactPhone(trimToNull(request.contactPhone()));

        return shareholderRepository.save(shareholder);
    }

    @Transactional
    @LogOperation(
            targetType = OperationTargetType.SHAREHOLDER,
            operationType = OperationType.STATUS_CHANGE,
            targetIdSpEL = "#result.id",
            targetNameSpEL = "#result.shareholderName"
    )
    public Shareholder updateShareholderStatus(Long shareholderId, String shareholderStatus) {
        if (shareholderId == null || shareholderId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "股东不存在", HttpStatus.BAD_REQUEST);
        }

        Shareholder shareholder = shareholderRepository.findById(shareholderId)
                .orElseThrow(() -> new BusinessException("SHAREHOLDER_NOT_FOUND", "股东不存在", HttpStatus.NOT_FOUND));

        shareholder.setShareholderStatus(resolveShareholderStatus(shareholderStatus));
        return shareholderRepository.save(shareholder);
    }

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
