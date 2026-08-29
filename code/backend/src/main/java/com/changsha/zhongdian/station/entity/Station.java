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
@Table(name = "biz_station")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_code", nullable = false, length = 32)
    private String stationCode;

    @Column(name = "station_name", nullable = false, length = 128)
    private String stationName;

    @Column(name = "platform_item_id", nullable = false)
    private Long platformItemId;

    @Column(name = "platform_station_code", length = 64)
    private String platformStationCode;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "contact_name", length = 64)
    private String contactName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "station_status_item_id", nullable = false)
    private Long stationStatusItemId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public Long getPlatformItemId() {
        return platformItemId;
    }

    public void setPlatformItemId(Long platformItemId) {
        this.platformItemId = platformItemId;
    }

    public String getPlatformStationCode() {
        return platformStationCode;
    }

    public void setPlatformStationCode(String platformStationCode) {
        this.platformStationCode = platformStationCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Long getStationStatusItemId() {
        return stationStatusItemId;
    }

    public void setStationStatusItemId(Long stationStatusItemId) {
        this.stationStatusItemId = stationStatusItemId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
