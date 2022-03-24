package com.github.mdvinyaninov.tools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class
})
@EnableAsync
public class AgentApp {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AgentApp.class);
        app.setAddCommandLineProperties(true);
        app.run(args);
    }
}
