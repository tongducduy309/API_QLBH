package com.gener.qlbh.database;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;

@Configuration
public class SqliteBootstrapConfig {

    @Bean
    ApplicationRunner sqliteEnsurePath(Environment env) {
        return args -> {
            String jdbc = env.getProperty("spring.datasource.url");
            if (jdbc != null && jdbc.startsWith("jdbc:sqlite:")) {
                String path = jdbc.substring("jdbc:sqlite:".length())
                        .replace("file:", ""); // phòng trường hợp dùng file:
                File dbFile = new File(path);
                File parent = dbFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs(); // TẠO THƯ MỤC CHA
                }
                if (!dbFile.exists()) {
                    dbFile.createNewFile(); // hoặc copy seed từ classpath nếu bạn muốn
                }
            }
        };
    }
}

