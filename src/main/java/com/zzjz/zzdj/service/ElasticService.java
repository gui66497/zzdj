package com.zzjz.zzdj.service;

import java.util.Map;

/**
 * @author 房桂堂
 * @description ElasticService
 * @date 2018/12/1 17:40
 */
public interface ElasticService {

    /**
     * 获取最近一次的nmap记录
     * @return 记录
     */
    Map<String, Boolean> getAllNmap();
}
