package com.min.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // @Scheduled 어노테이션이 동작하도록 스케줄링 기능을 활성화한다.
public class TroublelogBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TroublelogBackendApplication.class, args);
    }

}
