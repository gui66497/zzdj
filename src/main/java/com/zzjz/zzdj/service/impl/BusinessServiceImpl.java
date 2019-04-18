package com.zzjz.zzdj.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.zzjz.zzdj.bean.Business;
import com.zzjz.zzdj.service.BusinessService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author 房桂堂
 * @description BusinessServiceImpl
 * @date 2019/1/22 16:38
 */
@Service
public class BusinessServiceImpl implements BusinessService {

    /**
     * 业务信息json文件名
     */
    @Value("${BUSINESS_FILENAME}")
    String businessName;

    @Override
    public List<Business> getAllBusiness() {
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray array;
        array = (JsonArray) parser.parse(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(businessName)));
        return gson.fromJson(array, new TypeToken<List<Business>>(){}.getType());
    }

    @Override
    public List<String> getIpListByName(String businessName) {
        List<Business> businesses = getAllBusiness();
        for (Business b : businesses) {
            if (businessName.equals(b.getBusinessName())) {
                return b.getIpList();
            }
        }
        return null;
    }

    @Override
    public String getLocationByName(String businessName) {
        List<Business> businesses = getAllBusiness();
        for (Business b : businesses) {
            if (businessName.equals(b.getBusinessName())) {
                String firstIp = b.getIpList().get(0);
                if (firstIp.contains("10.1.242")) {
                    return "石家庄";
                } else if (firstIp.contains("10.1.230")) {
                    return "北京";
                }
            }
        }
        return "未知";
    }

}
