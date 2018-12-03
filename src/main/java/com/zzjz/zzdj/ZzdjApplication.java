package com.zzjz.zzdj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZzdjApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZzdjApplication.class, args);
    }
}
