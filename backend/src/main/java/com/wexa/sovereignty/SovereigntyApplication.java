package com.wexa.sovereignty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SovereigntyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SovereigntyApplication.class, args);
    }
}
