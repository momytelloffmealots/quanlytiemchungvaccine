package com.example.vaccinationsystem.service;

import com.example.vaccinationsystem.dao.AccountDao;
import com.example.vaccinationsystem.dto.AccountCreateRequest;
import com.example.vaccinationsystem.dto.AccountInfoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {
    private final AccountDao accountDao;

    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public List<AccountInfoDTO> getAllAccounts() {
        return accountDao.findAllWithDetails();
    }

    @Transactional
    public String createAccount(AccountCreateRequest request) {
        // 1. Generate Account ID
        String lastAcctId = accountDao.getLatestAccountId();
        String newAcctId = generateNextId(lastAcctId, "AC");

        // 2. Insert Account
        accountDao.insertAccount(newAcctId, request.getUsername(), request.getPassword(), request.getEmail(), request.getAuthority());

        // 3. Generate Employee ID and Insert into Role Table
        String role = request.getAuthority();
        String table = "";
        String idCol = "";
        String prefix = "";

        switch (role) {
            case "DOCTOR" -> { table = "DOCTOR"; idCol = "DOCTOR_ID"; prefix = "BS"; }
            case "CASHIER" -> { table = "CASHIER"; idCol = "CASHIER_ID"; prefix = "TN"; }
            case "INVENTORY_MANAGER" -> { table = "INVENTORY_MANAGER"; idCol = "INVENTORY_MANAGER_ID"; prefix = "QK"; }
            case "ADMINISTRATOR" -> { table = "ADMINISTRATOR"; idCol = "ADMINISTRATOR_ID"; prefix = "QT"; }
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        }

        String lastEmpId = accountDao.getLatestEmployeeId(table, idCol);
        String newEmpId = generateNextId(lastEmpId, prefix);

        accountDao.insertEmployeeRow(table, idCol, newEmpId, newAcctId, request.getName());

        return newAcctId;
    }

    @Transactional
    public void updateAccount(String id, AccountCreateRequest request) {
        // 1. Get current account info to check for role change
        List<AccountInfoDTO> all = accountDao.findAllWithDetails();
        AccountInfoDTO current = all.stream()
                .filter(a -> a.getAccountId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));

        String oldRole = current.getAuthority();
        String newRole = request.getAuthority();

        // 2. Update Email in ACCOUNT table
        accountDao.updateAccount(id, request.getEmail());

        if (newRole != null && !newRole.equals(oldRole)) {
            // ROLE CHANGED: Migrate between tables
            
            // Delete from old table
            String oldTable = getTableName(oldRole);
            if (!oldTable.isEmpty()) {
                accountDao.deleteFromRoleTable(oldTable, id);
            }

            // Insert into new table
            String newTable = getTableName(newRole);
            String idCol = getIdColumn(newRole);
            String prefix = getPrefix(newRole);

            String lastEmpId = accountDao.getLatestEmployeeId(newTable, idCol);
            String newEmpId = generateNextId(lastEmpId, prefix);

            accountDao.insertEmployeeRow(newTable, idCol, newEmpId, id, request.getName());
            
            // Update AUTHORITY in ACCOUNT table
            accountDao.updateAuthority(id, newRole);
        } else {
            // ROLE UNCHANGED: Just update name in the existing table
            String table = getTableName(oldRole);
            if (!table.isEmpty()) {
                accountDao.updateEmployeeName(table, id, request.getName());
            }
        }
    }

    private String getTableName(String role) {
        return switch (role) {
            case "DOCTOR" -> "DOCTOR";
            case "CASHIER" -> "CASHIER";
            case "INVENTORY_MANAGER" -> "INVENTORY_MANAGER";
            case "ADMINISTRATOR" -> "ADMINISTRATOR";
            default -> "";
        };
    }

    private String getIdColumn(String role) {
        return switch (role) {
            case "DOCTOR" -> "DOCTOR_ID";
            case "CASHIER" -> "CASHIER_ID";
            case "INVENTORY_MANAGER" -> "INVENTORY_MANAGER_ID";
            case "ADMINISTRATOR" -> "ADMINISTRATOR_ID";
            default -> "EMPLOYEE_ID";
        };
    }

    private String getPrefix(String role) {
        return switch (role) {
            case "DOCTOR" -> "BS";
            case "CASHIER" -> "TN";
            case "INVENTORY_MANAGER" -> "QK";
            case "ADMINISTRATOR" -> "QT";
            default -> "EM";
        };
    }

    @Transactional
    public void deleteAccount(String id) {
        accountDao.deleteAccountCascade(id);
    }

    public List<AccountInfoDTO> searchStaff(String role, String keyword) {
        return accountDao.searchStaff(role, keyword);
    }

    private String generateNextId(String currentId, String prefix) {
        // currentId format: PRE001
        String numericPart = currentId.substring(prefix.length());
        int nextNum = Integer.parseInt(numericPart) + 1;
        return String.format("%s%03d", prefix, nextNum);
    }
}
