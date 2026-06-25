package com.pat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.mybatis.spring.annotation.MapperScan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
@MapperScan("com.pat.**.mapper")
public class DemoApplication {

    public static void main(String[] args) {
        loadLocalEnv();
        SpringApplication.run(DemoApplication.class, args);
    }

    private static void loadLocalEnv() {
        List<Path> candidates = List.of(
                Path.of(".env"),
                Path.of("pet_backend", ".env")
        );
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                loadEnvFile(candidate);
            }
        }
    }

    private static void loadEnvFile(Path path) {
        try {
            for (String rawLine : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                    continue;
                }
                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                String value = stripQuotes(parts[1].trim());
                if (!key.isEmpty() && System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ignored) {
            // Local .env files are optional. Missing or unreadable files fall back to OS env.
        }
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

}
