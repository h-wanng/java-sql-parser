package com.wang.sqltest;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SqlTestApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SqlTestApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
        System.out.println("Started!");
    }

}
