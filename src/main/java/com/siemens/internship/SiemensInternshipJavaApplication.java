package com.siemens.internship;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SiemensInternshipJavaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiemensInternshipJavaApplication.class, args);
    }
}