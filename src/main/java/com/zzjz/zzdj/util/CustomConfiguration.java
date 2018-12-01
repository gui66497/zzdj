package com.zzjz.zzdj.util;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author 房桂堂
 * @description 配置Gson作为Controller的转换器
 * @date 2018/11/30 11:04
 */
@Configuration
public class CustomConfiguration {

    @Bean
    public HttpMessageConverters customConverters() {
        Collection<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        messageConverters.add(gsonHttpMessageConverter);
        return new HttpMessageConverters(true, messageConverters);
    }

}
