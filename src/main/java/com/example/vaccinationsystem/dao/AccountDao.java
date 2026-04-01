package com.example.vaccinationsystem.dao;

import com.example.vaccinationsystem.dto.AccountInfoDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountDao {
    private final JdbcTemplate jdbcTemplate;

    public AccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<AccountRow> ACCOUNT_ROW_MAPPER = (rs, rowNum) -> {
        AccountRow row = new AccountRow();
        row.setAccountId(rs.getString("ACCOUNT_ID"));
        row.setAuthority(rs.getString("AUTHORITY"));
        row.setUsername(rs.getString("USERNAME"));
        row.setPassword(rs.getString("PASSWORD"));
        row.setEmail(rs.getString("EMAIL"));
        return row;
    };

    public Optional<AccountRow> findByUsername(String username) {
        String sql = """
                SELECT ACCOUNT_ID, AUTHORITY, USERNAME, PASSWORD, EMAIL
                FROM ACCOUNT
                WHERE USERNAME = ?
                """;
        List<AccountRow> rows = jdbcTemplate.query(sql, ACCOUNT_ROW_MAPPER, username);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(rows.get(0));
    }

    public Optional<String> findDoctorIdByAccountId(String accountId) {
        String sql = "SELECT DOCTOR_ID FROM DOCTOR WHERE ACCOUNT_ID = ?";
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1), accountId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public Optional<String> findCashierIdByAccountId(String accountId) {
        String sql = "SELECT CASHIER_ID FROM CASHIER WHERE ACCOUNT_ID = ?";
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1), accountId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public Optional<String> findInventoryManagerIdByAccountId(String accountId) {
        String sql = "SELECT INVENTORY_MANAGER_ID FROM INVENTORY_MANAGER WHERE ACCOUNT_ID = ?";
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1), accountId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public Optional<String> findAdministratorIdByAccountId(String accountId) {
        String sql = "SELECT ADMINISTRATOR_ID FROM ADMINISTRATOR WHERE ACCOUNT_ID = ?";
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1), accountId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public List<AccountInfoDTO> findAllWithDetails() {
        String sql = """
                SELECT DISTINCT a.ACCOUNT_ID, a.AUTHORITY, a.USERNAME, a.EMAIL,
                       CASE a.AUTHORITY
                         WHEN 'DOCTOR' THEN d.DOCTOR_ID
                         WHEN 'CASHIER' THEN c.CASHIER_ID
                         WHEN 'INVENTORY_MANAGER' THEN i.INVENTORY_MANAGER_ID
                         WHEN 'ADMINISTRATOR' THEN m.ADMINISTRATOR_ID
                         ELSE NULL
                       END as EMP_ID,
                       CASE a.AUTHORITY
                         WHEN 'DOCTOR' THEN d.NAME
                         WHEN 'CASHIER' THEN c.NAME
                         WHEN 'INVENTORY_MANAGER' THEN i.NAME
                         WHEN 'ADMINISTRATOR' THEN m.NAME
                         ELSE NULL
                       END as EMP_NAME
                FROM ACCOUNT a
                LEFT JOIN DOCTOR d ON a.ACCOUNT_ID = d.ACCOUNT_ID
                LEFT JOIN CASHIER c ON a.ACCOUNT_ID = c.ACCOUNT_ID
                LEFT JOIN INVENTORY_MANAGER i ON a.ACCOUNT_ID = i.ACCOUNT_ID
                LEFT JOIN ADMINISTRATOR m ON a.ACCOUNT_ID = m.ACCOUNT_ID
                ORDER BY a.ACCOUNT_ID DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            AccountInfoDTO dto = new AccountInfoDTO();
            dto.setAccountId(rs.getString("ACCOUNT_ID"));
            dto.setAuthority(rs.getString("AUTHORITY"));
            dto.setUsername(rs.getString("USERNAME"));
            dto.setEmail(rs.getString("EMAIL"));
            dto.setEmployeeId(rs.getString("EMP_ID"));
            dto.setEmployeeName(rs.getString("EMP_NAME"));
            return dto;
        });
    }

    public String getLatestAccountId() {
        String sql = "SELECT ACCOUNT_ID FROM ACCOUNT ORDER BY ACCOUNT_ID DESC LIMIT 1";
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1));
        return rows.isEmpty() ? "AC000" : rows.get(0);
    }

    public String getLatestEmployeeId(String table, String idColumn) {
        String sql = "SELECT " + idColumn + " FROM " + table + " ORDER BY " + idColumn + " DESC LIMIT 1";
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1));
        if (rows.isEmpty()) {
           if (table.equals("DOCTOR")) return "BS000";
           if (table.equals("CASHIER")) return "TN000";
           if (table.equals("INVENTORY_MANAGER")) return "QK000";
           if (table.equals("ADMINISTRATOR")) return "QT000";
           return "EM000";
        }
        return rows.get(0);
    }

    public void insertAccount(String id, String username, String password, String email, String authority) {
        String sql = "INSERT INTO ACCOUNT (ACCOUNT_ID, USERNAME, PASSWORD, EMAIL, AUTHORITY) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, id, username, password, email, authority);
    }

    public void insertEmployeeRow(String table, String idCol, String empId, String acctId, String name) {
        // Assume role tables have at least ID and ACCOUNT_ID. Using NAME if exists.
        String sql = "INSERT INTO " + table + " (" + idCol + ", ACCOUNT_ID, NAME) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, empId, acctId, name);
    }

    public void updateAccount(String id, String email, String password) {
        if (password != null && !password.isEmpty()) {
            jdbcTemplate.update("UPDATE ACCOUNT SET EMAIL = ?, PASSWORD = ? WHERE ACCOUNT_ID = ?", email, password, id);
        } else {
            jdbcTemplate.update("UPDATE ACCOUNT SET EMAIL = ? WHERE ACCOUNT_ID = ?", email, id);
        }
    }

    public void updateAuthority(String id, String authority) {
        jdbcTemplate.update("UPDATE ACCOUNT SET AUTHORITY = ? WHERE ACCOUNT_ID = ?", authority, id);
    }

    public void updateEmployeeName(String table, String accountId, String name) {
        String sql = "UPDATE " + table + " SET NAME = ? WHERE ACCOUNT_ID = ?";
        jdbcTemplate.update(sql, name, accountId);
    }

    public void deleteFromRoleTable(String table, String accountId) {
        String sql = "DELETE FROM " + table + " WHERE ACCOUNT_ID = ?";
        jdbcTemplate.update(sql, accountId);
    }

    public boolean isDoctorUsed(String id) {
        String sql = "SELECT 1 FROM VACCINATION_FORM WHERE DOCTOR_ID = ? LIMIT 1";
        return !jdbcTemplate.queryForList(sql, Integer.class, id).isEmpty();
    }

    public boolean isCashierUsed(String id) {
        String sqlVf = "SELECT 1 FROM VACCINATION_FORM WHERE CASHIER_ID = ? LIMIT 1";
        String sqlBill = "SELECT 1 FROM BILL WHERE CASHIER_ID = ? LIMIT 1";
        boolean inVf = !jdbcTemplate.queryForList(sqlVf, Integer.class, id).isEmpty();
        boolean inBill = !jdbcTemplate.queryForList(sqlBill, Integer.class, id).isEmpty();
        return inVf || inBill;
    }

    public boolean isInventoryManagerUsed(String id) {
        String sql = "SELECT 1 FROM VACCINE WHERE INVENTORY_MANAGER_ID = ? LIMIT 1";
        return !jdbcTemplate.queryForList(sql, Integer.class, id).isEmpty();
    }

    public void nullifyEmployeeReferences(String accountId) {
        // 1. Get role IDs first
        Optional<String> docId = findDoctorIdByAccountId(accountId);
        Optional<String> cashId = findCashierIdByAccountId(accountId);
        Optional<String> invId = findInventoryManagerIdByAccountId(accountId);

        // 2. Nullify references (Try-Catch to handle missing tables/columns)
        try {
            docId.ifPresent(id -> jdbcTemplate.update("UPDATE VACCINATION_FORM SET DOCTOR_ID = NULL WHERE DOCTOR_ID = ?", id));
        } catch (Exception ignored) {}
        
        try {
            cashId.ifPresent(id -> {
                jdbcTemplate.update("UPDATE VACCINATION_FORM SET CASHIER_ID = NULL WHERE CASHIER_ID = ?", id);
            });
        } catch (Exception ignored) {}
        
        try {
            cashId.ifPresent(id -> {
                jdbcTemplate.update("UPDATE BILL SET CASHIER_ID = NULL WHERE CASHIER_ID = ?", id);
            });
        } catch (Exception ignored) {}

        try {
            invId.ifPresent(id -> {
                jdbcTemplate.update("UPDATE VACCINE SET INVENTORY_MANAGER_ID = NULL WHERE INVENTORY_MANAGER_ID = ?", id);
            });
        } catch (Exception ignored) {}
    }

    public void deleteAccountCascade(String accountId) {
        // 1. Nullify references in Vaccination Form & Bill to avoid FK errors
        nullifyEmployeeReferences(accountId);

        // 2. Delete from role tables
        jdbcTemplate.update("DELETE FROM DOCTOR WHERE ACCOUNT_ID = ?", accountId);
        jdbcTemplate.update("DELETE FROM CASHIER WHERE ACCOUNT_ID = ?", accountId);
        jdbcTemplate.update("DELETE FROM INVENTORY_MANAGER WHERE ACCOUNT_ID = ?", accountId);
        jdbcTemplate.update("DELETE FROM ADMINISTRATOR WHERE ACCOUNT_ID = ?", accountId);
        
        // 3. Finally delete account
        jdbcTemplate.update("DELETE FROM ACCOUNT WHERE ACCOUNT_ID = ?", accountId);
    }

    public List<AccountInfoDTO> searchStaff(String role, String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT a.ACCOUNT_ID, a.AUTHORITY, a.USERNAME, a.EMAIL,
                       CASE a.AUTHORITY
                         WHEN 'DOCTOR' THEN d.DOCTOR_ID
                         WHEN 'CASHIER' THEN c.CASHIER_ID
                         WHEN 'INVENTORY_MANAGER' THEN i.INVENTORY_MANAGER_ID
                         WHEN 'ADMINISTRATOR' THEN m.ADMINISTRATOR_ID
                         ELSE NULL
                       END as EMP_ID,
                       CASE a.AUTHORITY
                         WHEN 'DOCTOR' THEN d.NAME
                         WHEN 'CASHIER' THEN c.NAME
                         WHEN 'INVENTORY_MANAGER' THEN i.NAME
                         WHEN 'ADMINISTRATOR' THEN m.NAME
                         ELSE NULL
                       END as EMP_NAME
                FROM ACCOUNT a
                LEFT JOIN DOCTOR d ON a.ACCOUNT_ID = d.ACCOUNT_ID
                LEFT JOIN CASHIER c ON a.ACCOUNT_ID = c.ACCOUNT_ID
                LEFT JOIN INVENTORY_MANAGER i ON a.ACCOUNT_ID = i.ACCOUNT_ID
                LEFT JOIN ADMINISTRATOR m ON a.ACCOUNT_ID = m.ACCOUNT_ID
                WHERE 1=1
                """);
        java.util.ArrayList<Object> params = new java.util.ArrayList<>();

        if (role != null && !role.isEmpty()) {
            sql.append(" AND a.AUTHORITY = ?");
            params.add(role);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (a.USERNAME LIKE ? OR COALESCE(d.NAME, c.NAME, i.NAME, m.NAME) LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        sql.append(" ORDER BY a.ACCOUNT_ID DESC");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            AccountInfoDTO dto = new AccountInfoDTO();
            dto.setAccountId(rs.getString("ACCOUNT_ID"));
            dto.setAuthority(rs.getString("AUTHORITY"));
            dto.setUsername(rs.getString("USERNAME"));
            dto.setEmail(rs.getString("EMAIL"));
            dto.setEmployeeId(rs.getString("EMP_ID"));
            dto.setEmployeeName(rs.getString("EMP_NAME"));
            return dto;
        }, params.toArray());
    }

    public static class AccountRow {
        private String accountId;
        private String authority;
        private String username;
        private String password;
        private String email;

        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public String getAuthority() { return authority; }
        public void setAuthority(String authority) { this.authority = authority; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}

