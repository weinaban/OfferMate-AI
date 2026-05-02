package com.offermate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.offermate.mapper")
@SpringBootApplication
public class SpringBootOffermateServeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootOffermateServeApplication.class, args);
    }

}
