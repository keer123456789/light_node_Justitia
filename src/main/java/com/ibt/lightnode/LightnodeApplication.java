package com.ibt.lightnode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LightnodeApplication {
    public static void main(String[] args) {
        SpringApplication.run(LightnodeApplication.class, args);
    }
}
