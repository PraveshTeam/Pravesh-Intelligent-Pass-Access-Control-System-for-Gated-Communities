package com.pravesh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PraveshApplication {
    public static void main(String[] args) {
        SpringApplication.run(PraveshApplication.class, args);
    }
}
