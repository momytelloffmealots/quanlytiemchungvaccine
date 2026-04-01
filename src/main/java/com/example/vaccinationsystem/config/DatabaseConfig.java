package com.example.vaccinationsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Value("${MYSQL_URL:}")
    private String mysqlUrl;

    @Value("${spring.datasource.url:}")
    private String defaultUrl;

    @Value("${spring.datasource.username:root}")
    private String defaultUsername;

    @Value("${spring.datasource.password:}")
    private String defaultPassword;

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        // Nếu Railway có cung cấp biến môi trường MYSQL_URL, chúng ta sẽ parse nó
        String envMysqlUrl = System.getenv("MYSQL_URL");
        if (envMysqlUrl == null || envMysqlUrl.trim().isEmpty()) {
            envMysqlUrl = mysqlUrl;
        }

        if (envMysqlUrl != null && !envMysqlUrl.trim().isEmpty()) {
            try {
                // Định dạng của MYSQL_URL trên Railway thường là:
                // mysql://username:password@host:port/database
                URI dbUri = new URI(envMysqlUrl);

                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                
                // Chuyển đổi sang định dạng JDBC
                String dbUrl = "jdbc:mysql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() 
                        + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

                return DataSourceBuilder.create()
                        .url(dbUrl)
                        .username(username)
                        .password(password)
                        .driverClassName("com.mysql.cj.jdbc.Driver")
                        .build();
            } catch (Exception e) {
                System.err.println("Lỗi khi phân tích MYSQL_URL từ Railway. Trở lại cấu hình mặc định: " + e.getMessage());
            }
        }

        // Nếu không có MYSQL_URL hoặc có lỗi, thử lấy từ cấu hình application.properties hoặc biến môi trường khác
        return DataSourceBuilder.create()
                .url(defaultUrl.isEmpty() ? "jdbc:mysql://localhost:3306/VACCINATION_SYSTEM?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true" : defaultUrl)
                .username(defaultUsername)
                .password(defaultPassword)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
}
