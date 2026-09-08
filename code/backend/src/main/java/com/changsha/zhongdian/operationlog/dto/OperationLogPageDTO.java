package com.changsha.zhongdian.operationlog.dto;

import java.util.List;

public class OperationLogPageDTO {
    
    private List<TimelineEventDTO> records;
    private Long total;
    private Integer page;
    private Integer size;
    
    public OperationLogPageDTO(List<TimelineEventDTO> records, Long total, Integer page, Integer size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
    }
    
    public List<TimelineEventDTO> getRecords() {
        return records;
    }
    
    public void setRecords(List<TimelineEventDTO> records) {
        this.records = records;
    }
    
    public Long getTotal() {
        return total;
    }
    
    public void setTotal(Long total) {
        this.total = total;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
}
