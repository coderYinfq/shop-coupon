package com.neu.couponapp.config;

import org.apache.dubbo.config.MetadataReportConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DubboConfig {
    @Bean
    public MetadataReportConfig metadataReportConfig() {
        MetadataReportConfig metadataReportConfig = new MetadataReportConfig();

        //通过metadataReportConfig对象可以设置dubbo-admin的相关参数
        metadataReportConfig.setAddress("zookeeper://127.0.0.1:2181");
        return metadataReportConfig;
    }
}
