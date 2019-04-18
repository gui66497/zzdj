package com.zzjz.zzdj.service;

import com.zzjz.zzdj.bean.Business;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author 房桂堂
 * @description BusinessService
 * @date 2019/1/22 16:38
 */
public interface BusinessService {

    /**
     * 通过配置文件获取当前所有系统
     * @return 所有系统
     * @throws FileNotFoundException FileNotFoundException
     */
    List<Business>  getAllBusiness() throws FileNotFoundException;

    /**
     * 通过系统名获取ip列表
     * @param businessName 系统名
     * @return ip列表
     */
    List<String> getIpListByName(String businessName);

    /**
     * 根据业务名称获取所属地区(242网段的为石家庄,230网段的为北京)
     * @param businessName 业务名
     * @return 结果
     */
    String getLocationByName(String businessName);
}
