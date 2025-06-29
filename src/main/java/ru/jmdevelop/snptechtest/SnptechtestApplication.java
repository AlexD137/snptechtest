package ru.jmdevelop.snptechtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SnptechtestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnptechtestApplication.class, args);
    }

}
