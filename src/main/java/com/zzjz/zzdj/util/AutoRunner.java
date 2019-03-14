package com.zzjz.zzdj.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(10)// 执行顺序 越小优先级越高
public class AutoRunner implements ApplicationRunner {

    /**
     * 根据ntop导出的json数据初始化 ip和mac对应关系
     */
    public static Map<String, String> ipMacMap = new HashMap<>();

    @Override
    public void run(ApplicationArguments args) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("exported_data.json");
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = (JsonObject) parser.parse(new InputStreamReader(stream));
        JsonObject objectHosts = jsonObject.getAsJsonObject("hosts");
        for(Map.Entry<String,JsonElement> entry : objectHosts.entrySet()){
            ipMacMap.put(entry.getKey(), entry.getValue().getAsJsonObject().get("mac").getAsString());
        }
    }
}
