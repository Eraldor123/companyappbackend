package com.companyapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // PŘIDÁNO
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync // FÁZE 3: Povoluje spuštění metod v samostatných vláknech
public class CompanyappApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanyappApplication.class, args);
    }
}