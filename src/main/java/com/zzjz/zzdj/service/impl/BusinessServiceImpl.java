package com.zzjz.zzdj.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.zzjz.zzdj.bean.Business;
import com.zzjz.zzdj.service.BusinessService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * @author 房桂堂
 * @description BusinessServiceImpl
 * @date 2019/1/22 16:38
 */
@Service
public class BusinessServiceImpl implements BusinessService {

    @Value(value="classpath:Business.json")
    private Resource resource;

    @Override
    public List<Business> getAllBusiness() {
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray array;
        try {
            ApplicationHome home = new ApplicationHome(getClass());
            //jar文件所在路径
            File jarFile = home.getSource();
            String businessPath = jarFile.getParentFile().toString() + File.separator + "Business.json";
            array = (JsonArray) parser.parse(new FileReader(ResourceUtils.getFile(businessPath)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

}
