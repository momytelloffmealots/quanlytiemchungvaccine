package com.example.vaccinationsystem.dao;

import com.example.vaccinationsystem.dto.VaccineDTO;
import com.example.vaccinationsystem.dto.VaccineExpiringDTO;
import com.example.vaccinationsystem.dto.VaccineTypeDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class VaccineDao {
    private final JdbcTemplate jdbcTemplate;

    public VaccineDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<VaccineDTO> VACCINE_ROW_MAPPER = (rs, rowNum) -> {
        VaccineDTO dto = new VaccineDTO();
        dto.setVaccineId(rs.getString("VACCINE_ID"));
        dto.setName(rs.getString("NAME"));
        dto.setManufacturer(rs.getString("MANUFACTURER"));
        dto.setProductionDate(toLocalDate(rs.getDate("PRODUCTION_DATE")));
        dto.setExpiryDate(toLocalDate(rs.getDate("EXPIRY_DATE")));
        dto.setLot(rs.getString("VACCINE_LOT"));
        dto.setQuantityAvailable(rs.getInt("QUANTITY_AVAILABLE"));
        dto.setPrice(rs.getBigDecimal("PRICE"));
        dto.setVaccineTypeId(rs.getString("VACCINE_TYPE_ID"));
        dto.setVaccineTypeName(rs.getString("VACCINE_TYPE"));
        dto.setDaysLeft(rs.getInt("DAYS_LEFT"));
        return dto;
    };

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    public List<VaccineDTO> findAllVaccine() {
        String sql = """
                SELECT
                    v.VACCINE_ID,
                    v.NAME,
                    v.MANUFACTURER,
                    v.PRODUCTION_DATE,
                    v.EXPIRY AS EXPIRY_DATE,
                    v.VACCINE_LOT,
                    v.QUANTITY_AVAILABLE,
                    v.PRICE,
                    vt.VACCINE_TYPE_ID,
                    vt.VACCINE_TYPE,
                    DATEDIFF(v.EXPIRY, CURDATE()) AS DAYS_LEFT
                FROM VACCINE v
                JOIN VACCINE_TYPE vt ON vt.VACCINE_TYPE_ID = v.VACCINE_TYPE_ID
                ORDER BY v.VACCINE_ID
                """;
        return jdbcTemplate.query(sql, VACCINE_ROW_MAPPER);
    }

    public List<VaccineTypeDTO> findAllVaccineTypes() {
        String sql = "SELECT VACCINE_TYPE_ID, VACCINE_TYPE FROM VACCINE_TYPE ORDER BY VACCINE_TYPE_ID";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            VaccineTypeDTO dto = new VaccineTypeDTO();
            dto.setVaccineTypeId(rs.getString("VACCINE_TYPE_ID"));
            dto.setVaccineTypeName(rs.getString("VACCINE_TYPE"));
            return dto;
        });
    }

    public List<VaccineExpiringDTO> findExpiringInDays(int days) {
        String sql = """
                SELECT
                    v.VACCINE_ID,
                    v.NAME,
                    v.MANUFACTURER,
                    v.PRODUCTION_DATE,
                    v.EXPIRY AS EXPIRY_DATE,
                    v.VACCINE_LOT,
                    v.QUANTITY_AVAILABLE,
                    v.PRICE,
                    vt.VACCINE_TYPE,
                    DATEDIFF(v.EXPIRY, CURDATE()) AS DAYS_LEFT
                FROM VACCINE v
                JOIN VACCINE_TYPE vt ON vt.VACCINE_TYPE_ID = v.VACCINE_TYPE_ID
                WHERE v.EXPIRY >= CURDATE()
                  AND DATEDIFF(v.EXPIRY, CURDATE()) < ?
                ORDER BY DAYS_LEFT ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            VaccineExpiringDTO dto = new VaccineExpiringDTO();
            dto.setVaccineId(rs.getString("VACCINE_ID"));
            dto.setName(rs.getString("NAME"));
            dto.setManufacturer(rs.getString("MANUFACTURER"));
            dto.setProductionDate(toLocalDate(rs.getDate("PRODUCTION_DATE")));
            dto.setExpiryDate(toLocalDate(rs.getDate("EXPIRY_DATE")));
            dto.setLot(rs.getString("VACCINE_LOT"));
            dto.setQuantityAvailable(rs.getInt("QUANTITY_AVAILABLE"));
            dto.setPrice(rs.getBigDecimal("PRICE"));
            dto.setVaccineTypeName(rs.getString("VACCINE_TYPE"));
            dto.setDaysLeft(rs.getInt("DAYS_LEFT"));
            return dto;
        }, days);
    }

    public List<VaccineDTO> searchVaccine(String keyword) {
        String k = "%" + keyword + "%";
        String sql = """
                SELECT
                    v.VACCINE_ID,
                    v.NAME,
                    v.MANUFACTURER,
                    v.PRODUCTION_DATE,
                    v.EXPIRY AS EXPIRY_DATE,
                    v.VACCINE_LOT,
                    v.QUANTITY_AVAILABLE,
                    v.PRICE,
                    vt.VACCINE_TYPE_ID,
                    vt.VACCINE_TYPE,
                    DATEDIFF(v.EXPIRY, CURDATE()) AS DAYS_LEFT
                FROM VACCINE v
                JOIN VACCINE_TYPE vt ON vt.VACCINE_TYPE_ID = v.VACCINE_TYPE_ID
                WHERE v.VACCINE_ID LIKE ?
                   OR v.NAME LIKE ?
                   OR v.MANUFACTURER LIKE ?
                   OR DATE_FORMAT(v.PRODUCTION_DATE, '%Y-%m-%d') LIKE ?
                   OR DATE_FORMAT(v.EXPIRY, '%Y-%m-%d') LIKE ?
                   OR v.VACCINE_LOT LIKE ?
                   OR CAST(v.QUANTITY_AVAILABLE AS CHAR) LIKE ?
                   OR CAST(v.PRICE AS CHAR) LIKE ?
                   OR vt.VACCINE_TYPE LIKE ?
                ORDER BY v.VACCINE_ID
                """;
        return jdbcTemplate.query(sql, VACCINE_ROW_MAPPER, k, k, k, k, k, k, k, k, k);
    }

    public List<VaccineExpiringDTO> searchExpiringVaccine(String keyword, int days) {
        String k = "%" + keyword + "%";
        String sql = """
                SELECT
                    v.VACCINE_ID,
                    v.NAME,
                    v.MANUFACTURER,
                    v.PRODUCTION_DATE,
                    v.EXPIRY AS EXPIRY_DATE,
                    v.VACCINE_LOT,
                    v.QUANTITY_AVAILABLE,
                    v.PRICE,
                    vt.VACCINE_TYPE,
                    DATEDIFF(v.EXPIRY, CURDATE()) AS DAYS_LEFT
                FROM VACCINE v
                JOIN VACCINE_TYPE vt ON vt.VACCINE_TYPE_ID = v.VACCINE_TYPE_ID
                WHERE v.EXPIRY >= CURDATE()
                  AND DATEDIFF(v.EXPIRY, CURDATE()) < ?
                  AND (
                        v.VACCINE_ID LIKE ?
                     OR v.NAME LIKE ?
                     OR v.MANUFACTURER LIKE ?
                     OR DATE_FORMAT(v.PRODUCTION_DATE, '%Y-%m-%d') LIKE ?
                     OR DATE_FORMAT(v.EXPIRY, '%Y-%m-%d') LIKE ?
                     OR v.VACCINE_LOT LIKE ?
                     OR CAST(v.QUANTITY_AVAILABLE AS CHAR) LIKE ?
                     OR CAST(v.PRICE AS CHAR) LIKE ?
                     OR vt.VACCINE_TYPE LIKE ?
                  )
                ORDER BY DAYS_LEFT ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            VaccineExpiringDTO dto = new VaccineExpiringDTO();
            dto.setVaccineId(rs.getString("VACCINE_ID"));
            dto.setName(rs.getString("NAME"));
            dto.setManufacturer(rs.getString("MANUFACTURER"));
            dto.setProductionDate(toLocalDate(rs.getDate("PRODUCTION_DATE")));
            dto.setExpiryDate(toLocalDate(rs.getDate("EXPIRY_DATE")));
            dto.setLot(rs.getString("VACCINE_LOT"));
            dto.setQuantityAvailable(rs.getInt("QUANTITY_AVAILABLE"));
            dto.setPrice(rs.getBigDecimal("PRICE"));
            dto.setVaccineTypeName(rs.getString("VACCINE_TYPE"));
            dto.setDaysLeft(rs.getInt("DAYS_LEFT"));
            return dto;
        }, days, k, k, k, k, k, k, k, k, k);
    }

    public Optional<BigDecimal> getVaccinePrice(String vaccineId) {
        String sql = "SELECT PRICE FROM VACCINE WHERE VACCINE_ID = ?";
        return jdbcTemplate.query(sql, (rs) -> {
            if (rs.next()) return Optional.ofNullable(rs.getBigDecimal(1));
            return Optional.<BigDecimal>empty();
        }, vaccineId);
    }

    public Optional<String> getVaccineName(String vaccineId) {
        String sql = "SELECT NAME FROM VACCINE WHERE VACCINE_ID = ?";
        List<String> rows = jdbcTemplate.queryForList(sql, String.class, vaccineId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public boolean isVaccineInStock(String vaccineId) {
        String sql = "SELECT QUANTITY_AVAILABLE FROM VACCINE WHERE VACCINE_ID = ?";
        List<Integer> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), vaccineId);
        return !rows.isEmpty() && rows.get(0) > 0;
    }

    public int getQuantityAvailable(String vaccineId) {
        String sql = "SELECT QUANTITY_AVAILABLE FROM VACCINE WHERE VACCINE_ID = ?";
        List<Integer> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), vaccineId);
        return rows.isEmpty() ? 0 : rows.get(0);
    }

    public List<VaccineDTO> searchAdvanced(String typeName, Integer maxQuantity, String lot, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    v.VACCINE_ID, v.NAME, v.MANUFACTURER, v.PRODUCTION_DATE,
                    v.EXPIRY AS EXPIRY_DATE, v.VACCINE_LOT, v.QUANTITY_AVAILABLE,
                    v.PRICE, vt.VACCINE_TYPE_ID, vt.VACCINE_TYPE,
                    DATEDIFF(v.EXPIRY, CURDATE()) AS DAYS_LEFT
                FROM VACCINE v
                JOIN VACCINE_TYPE vt ON vt.VACCINE_TYPE_ID = v.VACCINE_TYPE_ID
                WHERE 1=1
                """);
        java.util.ArrayList<Object> params = new java.util.ArrayList<>();

        if (typeName != null && !typeName.isEmpty()) {
            sql.append(" AND vt.VACCINE_TYPE = ?");
            params.add(typeName);
        }
        if (maxQuantity != null) {
            sql.append(" AND v.QUANTITY_AVAILABLE < ?");
            params.add(maxQuantity);
        }
        if (lot != null && !lot.isEmpty()) {
            sql.append(" AND v.VACCINE_LOT = ?");
            params.add(lot);
        }
        if (startDate != null) {
            sql.append(" AND v.PRODUCTION_DATE >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND v.PRODUCTION_DATE <= ?");
            params.add(Date.valueOf(endDate));
        }

        sql.append(" ORDER BY v.VACCINE_ID");
        return jdbcTemplate.query(sql.toString(), VACCINE_ROW_MAPPER, params.toArray());
    }

    public String getLatestVaccineId() {
        String sql = "SELECT VACCINE_ID FROM VACCINE ORDER BY VACCINE_ID DESC LIMIT 1";
        List<String> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1));
        return results.isEmpty() ? "VC000" : results.get(0);
    }

    public void insertVaccine(String id, VaccineDTO dto) {
        String sql = """
                INSERT INTO VACCINE (VACCINE_ID, NAME, MANUFACTURER, PRODUCTION_DATE, EXPIRY, VACCINE_LOT, QUANTITY_AVAILABLE, PRICE, VACCINE_TYPE_ID, INVENTORY_MANAGER_ID)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                id,
                dto.getName(),
                dto.getManufacturer(),
                dto.getProductionDate() != null ? Date.valueOf(dto.getProductionDate()) : null,
                dto.getExpiryDate() != null ? Date.valueOf(dto.getExpiryDate()) : null,
                dto.getLot(),
                dto.getQuantityAvailable(),
                dto.getPrice(),
                dto.getVaccineTypeId(),
                dto.getInventoryManagerId());
    }

    public Optional<VaccineTypeDTO> findVaccineTypeByName(String name) {
        String sql = "SELECT VACCINE_TYPE_ID, VACCINE_TYPE FROM VACCINE_TYPE WHERE VACCINE_TYPE = ?";
        List<VaccineTypeDTO> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            VaccineTypeDTO dto = new VaccineTypeDTO();
            dto.setVaccineTypeId(rs.getString("VACCINE_TYPE_ID"));
            dto.setVaccineTypeName(rs.getString("VACCINE_TYPE"));
            return dto;
        }, name);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public String getLatestVaccineTypeId() {
        String sql = "SELECT VACCINE_TYPE_ID FROM VACCINE_TYPE ORDER BY VACCINE_TYPE_ID DESC LIMIT 1";
        List<String> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1));
        return results.isEmpty() ? "VT000" : results.get(0);
    }

    public void insertVaccineType(String id, String name) {
        String sql = "INSERT INTO VACCINE_TYPE (VACCINE_TYPE_ID, VACCINE_TYPE) VALUES (?, ?)";
        jdbcTemplate.update(sql, id, name);
    }
}

