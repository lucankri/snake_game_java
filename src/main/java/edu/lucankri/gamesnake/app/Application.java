package edu.lucankri.gamesnake.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("edu.lucankri.gamesnake.configurations")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
