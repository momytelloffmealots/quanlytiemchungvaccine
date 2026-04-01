package com.example.vaccinationsystem.dao;

import com.example.vaccinationsystem.dto.VaccinationFormCreateRequest;
import com.example.vaccinationsystem.dto.VaccinationFormInfoDTO;
import com.example.vaccinationsystem.dto.VaccinationFormVaccineDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class VaccinationFormDao {
    private final JdbcTemplate jdbcTemplate;

    public VaccinationFormDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    public String getLastestFormId() {
        String sql = "SELECT VACCINATION_FORM_ID FROM VACCINATION_FORM ORDER BY VACCINATION_FORM_ID DESC LIMIT 1";
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1));
        return rows.isEmpty() ? "PT000" : rows.get(0);
    }

    public boolean existsById(String formId) {
        String sql = "SELECT 1 FROM VACCINATION_FORM WHERE VACCINATION_FORM_ID = ? LIMIT 1";
        List<Integer> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), formId);
        return !rows.isEmpty();
    }

    public void insertFormRow(String formId, VaccinationFormCreateRequest req) {
        String sql = """
                INSERT INTO VACCINATION_FORM (VACCINATION_FORM_ID, CUSTOMER_ID, DOCTOR_ID, CASHIER_ID, VACCINATION_DATE)
                VALUES (?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                formId,
                req.getCustomerId(),
                req.getDoctorId(),
                req.getCashierId(),
                Date.valueOf(req.getVaccinationDate())
        );
    }

    public void insertFormDetails(String formId, List<?> details) {
        // details type erasure is OK; we cast inside.
        String sql = """
                INSERT INTO VACCINATION_FORM_DETAIL
                  (VACCINATION_FORM_ID, VACCINE_ID, CNT, DOSE, RETENTION, PRICE)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        for (Object d : details) {
            var det = (com.example.vaccinationsystem.dto.VaccinationFormDetailCreateRequest) d;
            jdbcTemplate.update(sql,
                    formId,
                    det.getVaccineId(),
                    det.getCnt(),
                    det.getDose(),
                    Date.valueOf(det.getRetentionDate()),
                    det.getPrice()
            );
        }
    }

    public int decrementVaccineStock(String vaccineId, int cnt) {
        // Ensure non-negative stock by checking QUANTITY_AVAILABLE >= cnt.
        String sql = """
                UPDATE VACCINE
                SET QUANTITY_AVAILABLE = QUANTITY_AVAILABLE - ?
                WHERE VACCINE_ID = ? AND QUANTITY_AVAILABLE >= ?
                """;
        return jdbcTemplate.update(sql, cnt, vaccineId, cnt);
    }

    public List<VaccinationFormInfoDTO> getAllFormsInfo() {
        String sql = """
                SELECT
                    f.VACCINATION_FORM_ID,
                    c.CUSTOMER_ID,
                    c.NAME AS CUSTOMER_NAME,
                    c.DATE_OF_BIRTH,
                    c.GENDER,
                    c.BIOGRAPHY,
                    c.EMAIL,
                    c.PHONE_NUM,
                    d.NAME AS DOCTOR_NAME,
                    cash.NAME AS CASHIER_NAME,
                    f.VACCINATION_DATE
                FROM VACCINATION_FORM f
                JOIN CUSTOMER c ON c.CUSTOMER_ID = f.CUSTOMER_ID
                JOIN DOCTOR d ON d.DOCTOR_ID = f.DOCTOR_ID
                JOIN CASHIER cash ON cash.CASHIER_ID = f.CASHIER_ID
                ORDER BY f.VACCINATION_FORM_ID DESC
                """;

        RowMapper<VaccinationFormInfoDTO> mapper = (rs, rowNum) -> {
            VaccinationFormInfoDTO dto = new VaccinationFormInfoDTO();
            dto.setVaccinationFormId(rs.getString("VACCINATION_FORM_ID"));
            dto.setCustomerId(rs.getString("CUSTOMER_ID"));
            dto.setCustomerName(rs.getString("CUSTOMER_NAME"));
            dto.setCustomerBirthDate(toLocalDate(rs.getDate("DATE_OF_BIRTH")));
            dto.setGender(rs.getString("GENDER"));
            dto.setBiography(rs.getString("BIOGRAPHY"));
            dto.setEmail(rs.getString("EMAIL"));
            dto.setPhoneNum(rs.getString("PHONE_NUM"));
            dto.setDoctorName(rs.getString("DOCTOR_NAME"));
            dto.setCashierName(rs.getString("CASHIER_NAME"));
            dto.setVaccinationDate(toLocalDate(rs.getDate("VACCINATION_DATE")));
            return dto;
        };

        return jdbcTemplate.query(sql, mapper);
    }

    public List<VaccinationFormVaccineDTO> getVaccinesFromForm(String formId) {
        String sql = """
                SELECT
                    d.VACCINE_ID,
                    v.NAME AS VACCINE_NAME,
                    vt.VACCINE_TYPE,
                    v.MANUFACTURER,
                    d.CNT,
                    d.DOSE,
                    d.RETENTION,
                    d.PRICE
                FROM VACCINATION_FORM_DETAIL d
                JOIN VACCINE v ON v.VACCINE_ID = d.VACCINE_ID
                JOIN VACCINE_TYPE vt ON vt.VACCINE_TYPE_ID = v.VACCINE_TYPE_ID
                WHERE d.VACCINATION_FORM_ID = ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            VaccinationFormVaccineDTO dto = new VaccinationFormVaccineDTO();
            dto.setVaccineId(rs.getString("VACCINE_ID"));
            dto.setVaccineName(rs.getString("VACCINE_NAME"));
            dto.setVaccineTypeName(rs.getString("VACCINE_TYPE"));
            dto.setManufacturer(rs.getString("MANUFACTURER"));
            dto.setCnt(rs.getInt("CNT"));
            dto.setDose(rs.getInt("DOSE"));
            dto.setRetentionDate(toLocalDate(rs.getDate("RETENTION")));
            dto.setPrice(rs.getBigDecimal("PRICE"));
            return dto;
        }, formId);
    }

    public Optional<String> getCustomerNameFromForm(String formId) {
        String sql = """
                SELECT c.NAME
                FROM VACCINATION_FORM f
                JOIN CUSTOMER c ON c.CUSTOMER_ID = f.CUSTOMER_ID
                WHERE f.VACCINATION_FORM_ID = ?
                LIMIT 1
                """;
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1), formId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public boolean hasBillForForm(String formId) {
        String sql = "SELECT 1 FROM BILL WHERE VACCINATION_FORM_ID = ? LIMIT 1";
        List<Integer> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), formId);
        return !rows.isEmpty();
    }

    public List<VaccinationFormInfoDTO> searchForms(LocalDate retentionDate, Integer month) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    f.VACCINATION_FORM_ID, c.CUSTOMER_ID, c.NAME AS CUSTOMER_NAME,
                    c.DATE_OF_BIRTH, c.GENDER, c.BIOGRAPHY, c.EMAIL, c.PHONE_NUM,
                    d.NAME AS DOCTOR_NAME, cash.NAME AS CASHIER_NAME, f.VACCINATION_DATE
                FROM VACCINATION_FORM f
                JOIN CUSTOMER c ON c.CUSTOMER_ID = f.CUSTOMER_ID
                JOIN DOCTOR d ON d.DOCTOR_ID = f.DOCTOR_ID
                JOIN CASHIER cash ON cash.CASHIER_ID = f.CASHIER_ID
                """);
        
        if (retentionDate != null) {
            sql.append(" JOIN VACCINATION_FORM_DETAIL det ON f.VACCINATION_FORM_ID = det.VACCINATION_FORM_ID");
        }
        
        sql.append(" WHERE 1=1");
        java.util.ArrayList<Object> params = new java.util.ArrayList<>();

        if (retentionDate != null) {
            sql.append(" AND det.RETENTION = ?");
            params.add(Date.valueOf(retentionDate));
        }
        if (month != null) {
            sql.append(" AND MONTH(f.VACCINATION_DATE) = ?");
            params.add(month);
        }

        sql.append(" ORDER BY f.VACCINATION_FORM_ID DESC");
        
        RowMapper<VaccinationFormInfoDTO> mapper = (rs, rowNum) -> {
            VaccinationFormInfoDTO dto = new VaccinationFormInfoDTO();
            dto.setVaccinationFormId(rs.getString("VACCINATION_FORM_ID"));
            dto.setCustomerId(rs.getString("CUSTOMER_ID"));
            dto.setCustomerName(rs.getString("CUSTOMER_NAME"));
            dto.setDoctorName(rs.getString("DOCTOR_NAME"));
            dto.setCashierName(rs.getString("CASHIER_NAME"));
            dto.setVaccinationDate(toLocalDate(rs.getDate("VACCINATION_DATE")));
            return dto;
        };

        return jdbcTemplate.query(sql.toString(), mapper, params.toArray());
    }

    public void deleteForm(String formId) {
        // delete details first due to FK constraints
        jdbcTemplate.update("DELETE FROM VACCINATION_FORM_DETAIL WHERE VACCINATION_FORM_ID = ?", formId);
        jdbcTemplate.update("DELETE FROM VACCINATION_FORM WHERE VACCINATION_FORM_ID = ?", formId);
    }
}

