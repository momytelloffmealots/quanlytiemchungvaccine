package com.example.vaccinationsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.vaccinationsystem.util.DotenvLoader;

@SpringBootApplication
public class VaccinationSystemApplication {
    public static void main(String[] args) {
        // Load optional ".env" file so you don't need to export env vars manually.
        DotenvLoader.loadIfPresent();
        SpringApplication.run(VaccinationSystemApplication.class, args);
    }
}

