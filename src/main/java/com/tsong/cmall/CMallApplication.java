package com.tsong.cmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.tsong.cmall.dao")
@SpringBootApplication
public class CMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(CMallApplication.class, args);
    }

}
