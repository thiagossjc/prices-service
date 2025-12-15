package org.company.price;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.company.price"})
public class PricesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PricesServiceApplication.class, args);
    }
}