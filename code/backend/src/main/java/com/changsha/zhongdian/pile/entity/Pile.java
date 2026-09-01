package com.changsha.zhongdian.pile.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "biz_pile")
public class Pile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pile_code", nullable = false, length = 32)
    private String pileCode;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_type", nullable = false, length = 16)
    private ChargeType chargeType;

    @Column(name = "gun_count", nullable = false)
    private Integer gunCount;

    @Column(name = "pile_status_item_id", nullable = false)
    private Long pileStatusItemId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getPileCode() {
        return pileCode;
    }

    public void setPileCode(String pileCode) {
        this.pileCode = pileCode;
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public ChargeType getChargeType() {
        return chargeType;
    }

    public void setChargeType(ChargeType chargeType) {
        this.chargeType = chargeType;
    }

    public Integer getGunCount() {
        return gunCount;
    }

    public void setGunCount(Integer gunCount) {
        this.gunCount = gunCount;
    }

    public Long getPileStatusItemId() {
        return pileStatusItemId;
    }

    public void setPileStatusItemId(Long pileStatusItemId) {
        this.pileStatusItemId = pileStatusItemId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
