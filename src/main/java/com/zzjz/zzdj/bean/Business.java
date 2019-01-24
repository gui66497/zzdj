package com.zzjz.zzdj.bean;

import java.util.List;

/**
 * @author 房桂堂
 * @description Business
 * @date 2019/1/21 15:33
 */
public class Business {

    /**
     * 业务名
     */
    private String businessName;

    /**
     * 其下ip列表
     */
    List<String> ipList;

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }
}
