package com.itsvitaliio.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.File;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.itsvitaliio.backend.repositories")
@EntityScan(basePackages = "com.itsvitaliio.backend.models")
public class BackendApplication {
    private static final String IMAGE_DIR = "images/";

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    CommandLineRunner init() {
        System.out.println("Initializing...");
        return args -> {
            File directory = new File(IMAGE_DIR);
            if (!directory.exists()) {
                System.out.println("Image directory does not exist, creating...");
                directory.mkdirs();
            }
        };
    }
}
