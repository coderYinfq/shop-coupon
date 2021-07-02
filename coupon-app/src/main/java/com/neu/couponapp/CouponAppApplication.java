package com.neu.couponapp;

import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDubboConfig
//扫描  对外提供的服务
@DubboComponentScan({"com.neu.couponapp.service.impl","com.neu.userapp.service.impl"})
@MapperScan("com.neu.couponapp.mapper")
@EnableScheduling
public class CouponAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponAppApplication.class, args);
    }

}
