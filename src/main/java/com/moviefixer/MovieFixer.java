package com.moviefixer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MovieFixer {

    public static void main(String[] args) {
        SpringApplication.run(MovieFixer.class, args);
    }
}