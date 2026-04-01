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
        accountDao.updateAccount(id, request.getEmail());
        
        String table = switch (request.getAuthority()) {
            case "DOCTOR" -> "DOCTOR";
            case "CASHIER" -> "CASHIER";
            case "INVENTORY_MANAGER" -> "INVENTORY_MANAGER";
            case "ADMINISTRATOR" -> "ADMINISTRATOR";
            default -> "";
        };
        if (!table.isEmpty()) {
            accountDao.updateEmployeeName(table, id, request.getName());
        }
    }

    public void deleteAccount(String id) {
        accountDao.deleteAccountCascade(id);
    }

    public List<AccountInfoDTO> searchStaff(String role, String address, Integer birthYear, String certificate, String lotNumber) {
        return accountDao.searchStaff(role, address, birthYear, certificate, lotNumber);
    }

    private String generateNextId(String currentId, String prefix) {
        // currentId format: PRE001
        String numericPart = currentId.substring(prefix.length());
        int nextNum = Integer.parseInt(numericPart) + 1;
        return String.format("%s%03d", prefix, nextNum);
    }
}
