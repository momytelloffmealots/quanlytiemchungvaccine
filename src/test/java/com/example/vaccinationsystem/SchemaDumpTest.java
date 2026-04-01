package com.example.vaccinationsystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class SchemaDumpTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void dumpSchemas() {
        String[] tables = {"ACCOUNT", "DOCTOR", "CASHIER", "INVENTORY_MANAGER", "ADMINISTRATOR"};
        for (String table : tables) {
            System.out.println("====== TABLE: " + table + " ======");
            try {
                List<Map<String, Object>> columns = jdbcTemplate.queryForList("DESCRIBE " + table);
                for (Map<String, Object> col : columns) {
                    System.out.println("  " + col.get("Field") + " - " + col.get("Type") + " - Null=" + col.get("Null") + " - Key=" + col.get("Key"));
                }
            } catch (Exception e) {
                System.out.println("  Table might not exist: " + e.getMessage());
            }
        }
    }
}
