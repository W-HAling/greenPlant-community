package com.plantadoption;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 办公室绿植领养系统主启动类
 * 
 * @author PlantAdoption Team
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@MapperScan("com.plantadoption.mapper")
public class PlantAdoptionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlantAdoptionApplication.class, args);
    }
}
