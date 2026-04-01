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
        if(dob != null) dto.setDateOfBirth(dob.toLocalDate());
        dto.setPhoneNum(rs.getString("PHONE_NUM"));
        dto.setEmail(rs.getString("EMAIL"));
        dto.setAddress(rs.getString("BIOGRAPHY")); // Using biography column as address
        return dto;
    };

    public List<CustomerDTO> findAll() {
        String sql = "SELECT * FROM CUSTOMER ORDER BY CUSTOMER_ID DESC";
        return jdbcTemplate.query(sql, CUSTOMER_ROW_MAPPER);
    }

    public List<CustomerDTO> searchByName(String name) {
        // Query 18: SELECT CUSTOMER.CUSTOMER_ID, CUSTOMER.NAME FROM CUSTOMER WHERE CUSTOMER.NAME LIKE N'Phạm%';
        String sql = "SELECT * FROM CUSTOMER WHERE NAME LIKE ? ORDER BY NAME";
        return jdbcTemplate.query(sql, CUSTOMER_ROW_MAPPER, name + "%");
    }
}
