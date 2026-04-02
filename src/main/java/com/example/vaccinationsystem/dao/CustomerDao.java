package com.example.vaccinationsystem.dao;

import com.example.vaccinationsystem.dto.CustomerDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomerDao {
    private final JdbcTemplate jdbcTemplate;

    public CustomerDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<CustomerDTO> CUSTOMER_ROW_MAPPER = (rs, rowNum) -> {
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerId(rs.getString("CUSTOMER_ID"));
        dto.setName(rs.getString("NAME"));
        dto.setGender(rs.getString("GENDER"));
        java.sql.Date dob = rs.getDate("DATE_OF_BIRTH");
        if (dob != null)
            dto.setDateOfBirth(dob.toLocalDate());
        dto.setPhoneNum(rs.getString("PHONE_NUM"));
        dto.setEmail(rs.getString("EMAIL"));
        dto.setAddress(rs.getString("BIOGRAPHY")); // Using biography column as address
        dto.setVisitCount(rs.getInt("visitCount"));
        java.sql.Date lv = rs.getDate("lastVisit");
        if (lv != null) dto.setLastVisit(lv.toLocalDate());
        return dto;
    };

    public List<CustomerDTO> findAll() {
        String sql = """
                SELECT 
                    c.*, 
                    (SELECT COUNT(*) FROM VACCINATION_FORM f WHERE f.CUSTOMER_ID = c.CUSTOMER_ID) AS visitCount,
                    (SELECT MAX(VACCINATION_DATE) FROM VACCINATION_FORM f WHERE f.CUSTOMER_ID = c.CUSTOMER_ID) AS lastVisit
                FROM CUSTOMER c
                ORDER BY c.CUSTOMER_ID DESC
                """;
        return jdbcTemplate.query(sql, CUSTOMER_ROW_MAPPER);
    }

    public List<CustomerDTO> searchByName(String name) {
        String sql = """
                SELECT 
                    c.*, 
                    (SELECT COUNT(*) FROM VACCINATION_FORM f WHERE f.CUSTOMER_ID = c.CUSTOMER_ID) AS visitCount,
                    (SELECT MAX(VACCINATION_DATE) FROM VACCINATION_FORM f WHERE f.CUSTOMER_ID = c.CUSTOMER_ID) AS lastVisit
                FROM CUSTOMER c
                WHERE c.NAME LIKE ? 
                ORDER BY c.NAME
                """;
        return jdbcTemplate.query(sql, CUSTOMER_ROW_MAPPER, "%" + name + "%");
    }

    public String getLatestCustomerId() {
        String sql = "SELECT CUSTOMER_ID FROM CUSTOMER ORDER BY CUSTOMER_ID DESC LIMIT 1";
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1));
        return rows.isEmpty() ? "KH000" : rows.get(0);
    }

    public void insertCustomer(String id, CustomerDTO dto) {
        String sql = """
                INSERT INTO CUSTOMER (CUSTOMER_ID, NAME, GENDER, DATE_OF_BIRTH, PHONE_NUM, EMAIL, BIOGRAPHY)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                id,
                dto.getName(),
                dto.getGender(),
                dto.getDateOfBirth() != null ? java.sql.Date.valueOf(dto.getDateOfBirth()) : null,
                dto.getPhoneNum(),
                dto.getEmail(),
                dto.getAddress());
    }
}
