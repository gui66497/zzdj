package com.zzjz.zzdj.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.zzjz.zzdj.bean.Business;
import com.zzjz.zzdj.service.BusinessService;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

/**
 * @author 房桂堂
 * @description BusinessServiceImpl
 * @date 2019/1/22 16:38
 */
@Service
public class BusinessServiceImpl implements BusinessService {

    @Override
    public List<Business> getAllBusiness() {
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray array;
        try {
            array = (JsonArray) parser.parse(new FileReader(
                    ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX  + "Business.json")));
        } catch (FileNotFoundException e) {
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
