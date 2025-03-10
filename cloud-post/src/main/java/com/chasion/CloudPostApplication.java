package com.chasion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CloudPostApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudPostApplication.class, args);
    }

}