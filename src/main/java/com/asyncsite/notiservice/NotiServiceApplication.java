package com.asyncsite.notiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableDiscoveryClient
public class NotiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotiServiceApplication.class, args);
    }

}
