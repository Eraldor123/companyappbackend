package com.companyapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // <--- PŘIDÁNO: Říká Javě, že má poslouchat na načasované úlohy
public class CompanyappApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanyappApplication.class, args);
    }
}