package ru.astafev.test.cbr;

import org.dizitart.no2.Nitrite;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import ru.astafev.test.cbr.config.DBConfig;
import ru.astafev.test.cbr.service.PrintDispatcher;

@SpringBootApplication
@Import(DBConfig.class)
public class PrintDispatcherApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(PrintDispatcherApp.class, args);
    }

}
