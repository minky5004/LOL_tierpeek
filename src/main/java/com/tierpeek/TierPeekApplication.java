package com.tierpeek;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TierPeekApplication {

    public static void main(String[] args) {
        SpringApplication.run(TierPeekApplication.class, args);
    }
}
