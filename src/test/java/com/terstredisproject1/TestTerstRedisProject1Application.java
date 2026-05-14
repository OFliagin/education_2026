package com.terstredisproject1;

import org.springframework.boot.SpringApplication;

public class TestTerstRedisProject1Application {

    public static void main(String[] args) {
        SpringApplication.from(TerstRedisProject1Application::main).with(TestcontainersConfiguration.class).run(args);
    }

}
