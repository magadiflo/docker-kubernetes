package com.magadiflo.dk.business.domain.courses.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class DkMsCoursesApplication {

    public static void main(String[] args) {
        SpringApplication.run(DkMsCoursesApplication.class, args);
    }

}
