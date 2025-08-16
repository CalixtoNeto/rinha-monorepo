package com.rinhadebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RinhaDeBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(RinhaDeBackendApplication.class, args);
    }
}
