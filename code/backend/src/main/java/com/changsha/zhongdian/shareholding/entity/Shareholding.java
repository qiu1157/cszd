package com.changsha.zhongdian.shareholding.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "biz_shareholding")
public class Shareholding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 为什么只存主键ID不做实体关联：与 biz_pile.station_id 同口径，主档与关系解耦，由应用层维护引用一致性
    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Column(name = "shareholder_id", nullable = false)
    private Long shareholderId;

    @Column(name = "ownership_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal ownershipPercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "holding_status", nullable = false, length = 16)
    private HoldingStatus holdingStatus;

    @Column(name = "effective_at")
    private LocalDateTime effectiveAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 用于操作日志展示，不持久化到数据库
    @Transient
    private String shareholderName;

    public Long getId() {
        return id;
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public Long getShareholderId() {
        return shareholderId;
    }

    public void setShareholderId(Long shareholderId) {
        this.shareholderId = shareholderId;
    }

    public BigDecimal getOwnershipPercentage() {
        return ownershipPercentage;
    }

    public void setOwnershipPercentage(BigDecimal ownershipPercentage) {
        this.ownershipPercentage = ownershipPercentage;
    }

    public HoldingStatus getHoldingStatus() {
        return holdingStatus;
    }

    public void setHoldingStatus(HoldingStatus holdingStatus) {
        this.holdingStatus = holdingStatus;
    }

    public LocalDateTime getEffectiveAt() {
        return effectiveAt;
    }

    public void setEffectiveAt(LocalDateTime effectiveAt) {
        this.effectiveAt = effectiveAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getShareholderName() {
        return shareholderName;
    }

    public void setShareholderName(String shareholderName) {
        this.shareholderName = shareholderName;
    }
}