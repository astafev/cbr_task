package ru.astafev.test.cbr.config;

import org.dizitart.no2.Nitrite;
import org.springframework.context.annotation.Bean;

public class DBConfig {

    @Bean
    public Nitrite getDb() {
        Nitrite db = Nitrite.builder()
                .compressed()
                .filePath("./test.db")
                .openOrCreate();
        return db;
    }
}
