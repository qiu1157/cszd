package com.changsha.zhongdian.station.service;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.operationlog.annotation.LogOperation;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.entity.OperationType;
import com.changsha.zhongdian.operationlog.util.BeforeStateHolder;
import com.changsha.zhongdian.station.dto.StationCreateRequest;
import com.changsha.zhongdian.station.dto.StationStatusUpdateRequest;
import com.changsha.zhongdian.station.dto.StationUpdateRequest;
import com.changsha.zhongdian.station.entity.Station;
import com.changsha.zhongdian.station.entity.StationStatusLog;
import com.changsha.zhongdian.station.repository.StationRepository;
import com.changsha.zhongdian.station.repository.StationStatusLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StationCommandService {

    private static final String DEFAULT_STATION_STATUS_NAME = "签合同";

    private final StationRepository stationRepository;
    private final StationCodeGenerator stationCodeGenerator;
    private final SystemDictItemRepository dictItemRepository;
    private final StationStatusLogRepository stationStatusLogRepository;

    public StationCommandService(
            StationRepository stationRepository,
            StationCodeGenerator stationCodeGenerator,
            SystemDictItemRepository dictItemRepository,
            StationStatusLogRepository stationStatusLogRepository
    ) {
        this.stationRepository = stationRepository;
        this.stationCodeGenerator = stationCodeGenerator;
        this.dictItemRepository = dictItemRepository;
        this.stationStatusLogRepository = stationStatusLogRepository;
    }

    @LogOperation(
            targetType = OperationTargetType.STATION,
            operationType = OperationType.CREATE,
            targetIdSpEL = "#result.stationCode",
            targetNameSpEL = "#result.stationName",
            stationIdSpEL = "#result.id"
    )
    public Station createStation(StationCreateRequest request) {
        String stationName = requireNonBlank(request.stationName(), "请输入充电站名称");
        Long platformItemId = requirePositiveId(request.platformItemId(), "请选择所在平台");

        SystemDictItem platformItem = resolveEnabledPlatform(platformItemId);

        SystemDictItem contractStatus = dictItemRepository
                .findByTypeTypeCodeAndItemNameAndItemStatus(
                        DictionaryTypeCode.STATION_STATUS,
                        DEFAULT_STATION_STATUS_NAME,
                        DictionaryStatus.ENABLED
                )
                .orElseThrow(() -> new BusinessException("STATION_STATUS_NOT_FOUND", "默认充电站状态不存在", HttpStatus.BAD_REQUEST));

        String platformStationCode = trimToNull(request.platformStationCode());
        validatePlatformStationCodeUniqueness(platformItem.getId(), platformStationCode);

        Station station = new Station();
        station.setStationCode(stationCodeGenerator.generateNextCode());
        station.setStationName(stationName);
        station.setPlatformItemId(platformItem.getId());
        station.setPlatformStationCode(platformStationCode);
        station.setAddress(trimToNull(request.address()));
        station.setContactName(trimToNull(request.contactName()));
        station.setContactPhone(trimToNull(request.contactPhone()));
        station.setMeterAccountNumber(trimToNull(request.meterAccountNumber()));
        station.setStationStatusItemId(contractStatus.getId());

        return stationRepository.save(station);
    }

    @LogOperation(
            targetType = OperationTargetType.STATION,
            operationType = OperationType.UPDATE,
            targetIdSpEL = "#result.stationCode",
            targetNameSpEL = "#result.stationName",
            stationIdSpEL = "#result.id",
            captureBeforeState = true
    )
    public Station updateStation(Long stationId, StationUpdateRequest request) {
        if (stationId == null || stationId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "充电站不存在", HttpStatus.BAD_REQUEST);
        }

        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new BusinessException("STATION_NOT_FOUND", "充电站不存在", HttpStatus.NOT_FOUND));

        BeforeStateHolder.set(cloneStation(station));

        String stationName = requireNonBlank(request.stationName(), "请输入充电站名称");
        Long platformItemId = requirePositiveId(request.platformItemId(), "请选择所在平台");
        SystemDictItem platformItem = resolveEnabledPlatform(platformItemId);

        String platformStationCode = trimToNull(request.platformStationCode());
        validatePlatformStationCodeUniqueness(platformItem.getId(), platformStationCode, stationId);

        station.setStationName(stationName);
        station.setPlatformItemId(platformItem.getId());
        station.setPlatformStationCode(platformStationCode);
        station.setAddress(trimToNull(request.address()));
        station.setContactName(trimToNull(request.contactName()));
        station.setContactPhone(trimToNull(request.contactPhone()));
        station.setMeterAccountNumber(trimToNull(request.meterAccountNumber()));

        return stationRepository.save(station);
    }
    
    private Station cloneStation(Station source) {
        Station clone = new Station();
        clone.setStationCode(source.getStationCode());
        clone.setStationName(source.getStationName());
        clone.setPlatformItemId(source.getPlatformItemId());
        clone.setPlatformStationCode(source.getPlatformStationCode());
        clone.setAddress(source.getAddress());
        clone.setContactName(source.getContactName());
        clone.setContactPhone(source.getContactPhone());
        clone.setMeterAccountNumber(source.getMeterAccountNumber());
        clone.setStationStatusItemId(source.getStationStatusItemId());
        return clone;
    }

    @Transactional
    @LogOperation(
            targetType = OperationTargetType.STATION,
            operationType = OperationType.STATUS_CHANGE,
            targetIdSpEL = "#result.stationCode",
            targetNameSpEL = "#result.stationName",
            stationIdSpEL = "#result.id",
            remarkSpEL = "#request.remark"
    )
    public Station updateStationStatus(Long stationId, StationStatusUpdateRequest request, String operatorAccount) {
        if (stationId == null || stationId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "充电站不存在", HttpStatus.BAD_REQUEST);
        }

        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new BusinessException("STATION_NOT_FOUND", "充电站不存在", HttpStatus.NOT_FOUND));

        Long targetStatusItemId = requirePositiveId(request.stationStatusItemId(), "请选择新状态");
        SystemDictItem targetStatus = dictItemRepository
                .findByIdAndTypeTypeCodeAndItemStatus(targetStatusItemId, DictionaryTypeCode.STATION_STATUS, DictionaryStatus.ENABLED)
                .orElseThrow(() -> new BusinessException("STATION_STATUS_NOT_FOUND", "目标状态不存在或已停用", HttpStatus.BAD_REQUEST));

        Long fromStatusItemId = station.getStationStatusItemId();
        station.setStationStatusItemId(targetStatus.getId());
        stationRepository.save(station);

        StationStatusLog statusLog = new StationStatusLog();
        statusLog.setStationId(station.getId());
        statusLog.setFromStatusItemId(fromStatusItemId);
        statusLog.setToStatusItemId(targetStatus.getId());
        statusLog.setRemark(trimToNull(request.remark()));
        statusLog.setOperatorAccount(trimToNull(operatorAccount));
        stationStatusLogRepository.save(statusLog);

        return station;
    }

    private SystemDictItem resolveEnabledPlatform(Long platformItemId) {
        return dictItemRepository
                .findByIdAndTypeTypeCodeAndItemStatus(platformItemId, DictionaryTypeCode.PLATFORM, DictionaryStatus.ENABLED)
                .orElseThrow(() -> new BusinessException("PLATFORM_NOT_FOUND", "所选平台不存在或已停用", HttpStatus.BAD_REQUEST));
    }

    private void validatePlatformStationCodeUniqueness(Long platformItemId, String platformStationCode) {
        if (platformStationCode != null
                && stationRepository.existsByPlatformItemIdAndPlatformStationCode(platformItemId, platformStationCode)) {
            throw new BusinessException("STATION_PLATFORM_CODE_DUPLICATED", "同一平台下的平台站点编号不能重复", HttpStatus.BAD_REQUEST);
        }
    }

    private void validatePlatformStationCodeUniqueness(Long platformItemId, String platformStationCode, Long stationId) {
        if (platformStationCode != null
                && stationRepository.existsByPlatformItemIdAndPlatformStationCodeAndIdNot(
                platformItemId,
                platformStationCode,
                stationId
        )) {
            throw new BusinessException("STATION_PLATFORM_CODE_DUPLICATED", "同一平台下的平台站点编号不能重复", HttpStatus.BAD_REQUEST);
        }
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
        }
        return value.trim();
    }

    private Long requirePositiveId(Long value, String message) {
        if (value == null || value <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalizedValue = value.trim();
        return normalizedValue.isEmpty() ? null : normalizedValue;
    }
}
