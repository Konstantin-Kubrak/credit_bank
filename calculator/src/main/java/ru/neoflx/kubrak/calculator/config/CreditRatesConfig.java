package ru.neoflx.kubrak.calculator.config;

import lombok.Data;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource("classpath:credit-rates.properties")
public class CreditRatesConfig {

}