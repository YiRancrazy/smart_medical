package com.yirancrazy.smartmedical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Yiran
 */
@EnableScheduling
@SpringBootApplication
public class SmartMedicalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartMedicalApplication.class, args);
    }

}
