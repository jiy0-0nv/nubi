package com.nubi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NubiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NubiApplication.class, args);
    }

}
