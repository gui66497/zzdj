package com.zzjz.zzdj.bean;

/**
 * @author 房桂堂
 * @description UnRegularFlow
 * @date 2019/1/8 14:55
 */
public class UnRegularFlow {

    private String timestamp;

    private String srcIp;

    private String dstIp;

    private String service;

    private String startTime;

    private String endTime;

    private int count;

    private Long outBytes;

    private Long inBytes;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Long getOutBytes() {
        return outBytes;
    }

    public void setOutBytes(Long outBytes) {
        this.outBytes = outBytes;
    }

    public Long getInBytes() {
        return inBytes;
    }

    public void setInBytes(Long inBytes) {
        this.inBytes = inBytes;
    }
}
