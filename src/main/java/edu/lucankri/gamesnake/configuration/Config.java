package edu.lucankri.gamesnake.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("edu.lucankri.gamesnake")
@PropertySource("classpath:application.properties")
public class Config {
}
