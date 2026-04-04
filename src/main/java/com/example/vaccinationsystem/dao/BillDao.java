package com.example.vaccinationsystem.dao;

import com.example.vaccinationsystem.dto.BillCreateRequest;
import com.example.vaccinationsystem.dto.BillInfoDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public class BillDao {
    private final JdbcTemplate jdbcTemplate;

    public BillDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    public String getLastestBillId() {
        String sql = "SELECT BILL_ID FROM BILL ORDER BY BILL_ID DESC LIMIT 1";
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1));
        return rows.isEmpty() ? "HD000" : rows.get(0);
    }

    public boolean hasBillForForm(String formId) {
        String sql = "SELECT 1 FROM BILL WHERE VACCINATION_FORM_ID = ? LIMIT 1";
        List<Integer> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), formId);
        return !rows.isEmpty();
    }

    public java.math.BigDecimal computeTotalAmountFromDetails(String formId) {
        String sql = "SELECT SUM(PRICE) FROM VACCINATION_FORM_DETAIL WHERE VACCINATION_FORM_ID = ?";
        java.math.BigDecimal sum = jdbcTemplate.queryForObject(sql, java.math.BigDecimal.class, formId);
        return sum != null ? sum : java.math.BigDecimal.ZERO;
    }

    public void insertBill(String billId, BillCreateRequest req) {
        String sql = """
                INSERT INTO BILL (BILL_ID, DISCOUNT, DUE_DATE, TOTAL_AMOUNT, CASHIER_ID, VACCINATION_FORM_ID)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                billId,
                req.getDiscount(),
                Date.valueOf(req.getDueDate()),
                req.getTotalAmount(),
                req.getCashierId(),
                req.getVaccinationFormId()
        );
    }

    public List<BillInfoDTO> findAllBillInfo() {
        String sql = """
                SELECT
                    b.BILL_ID,
                    b.VACCINATION_FORM_ID,
                    b.DISCOUNT,
                    b.DUE_DATE,
                    b.TOTAL_AMOUNT,
                    b.CASHIER_ID,
                    cash.NAME AS CASHIER_NAME,
                    c.NAME AS CUSTOMER_NAME,
                    f.VACCINATION_DATE
                FROM BILL b
                JOIN VACCINATION_FORM f ON f.VACCINATION_FORM_ID = b.VACCINATION_FORM_ID
                JOIN CUSTOMER c ON c.CUSTOMER_ID = f.CUSTOMER_ID
                LEFT JOIN CASHIER cash ON cash.CASHIER_ID = b.CASHIER_ID
                ORDER BY b.BILL_ID DESC
                """;

        RowMapper<BillInfoDTO> mapper = (rs, rowNum) -> {
            BillInfoDTO dto = new BillInfoDTO();
            dto.setBillId(rs.getString("BILL_ID"));
            dto.setVaccinationFormId(rs.getString("VACCINATION_FORM_ID"));
            dto.setDiscount(rs.getBigDecimal("DISCOUNT"));
            dto.setDueDate(toLocalDate(rs.getDate("DUE_DATE")));
            dto.setTotalAmount(rs.getBigDecimal("TOTAL_AMOUNT"));
            dto.setCashierId(rs.getString("CASHIER_ID"));
            dto.setCashierName(rs.getString("CASHIER_NAME"));
            dto.setCustomerName(rs.getString("CUSTOMER_NAME"));
            dto.setVaccinationDate(toLocalDate(rs.getDate("VACCINATION_DATE")));
            return dto;
        };
        return jdbcTemplate.query(sql, mapper);
    }
}

