package com.companyapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CompanyappApplication {

    // Hlavní metoda, která spouští Spring Boot aplikaci. Používá SpringApplication.run() k inicializaci a spuštění celé aplikace.
    public static void main(String[] args) {
        SpringApplication.run(CompanyappApplication.class, args);
    }

}
