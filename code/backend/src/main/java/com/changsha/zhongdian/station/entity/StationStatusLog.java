package com.changsha.zhongdian.station.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "biz_station_status_log")
public class StationStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Column(name = "from_status_item_id", nullable = false)
    private Long fromStatusItemId;

    @Column(name = "to_status_item_id", nullable = false)
    private Long toStatusItemId;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "operator_account", length = 32)
    private String operatorAccount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public Long getFromStatusItemId() {
        return fromStatusItemId;
    }

    public void setFromStatusItemId(Long fromStatusItemId) {
        this.fromStatusItemId = fromStatusItemId;
    }

    public Long getToStatusItemId() {
        return toStatusItemId;
    }

    public void setToStatusItemId(Long toStatusItemId) {
        this.toStatusItemId = toStatusItemId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getOperatorAccount() {
        return operatorAccount;
    }

    public void setOperatorAccount(String operatorAccount) {
        this.operatorAccount = operatorAccount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
