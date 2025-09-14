package com.yuanc.yuanpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.yuanc.yuanpicturebackend.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class YuanPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuanPictureBackendApplication.class, args);
    }

}
