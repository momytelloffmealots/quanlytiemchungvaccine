package com.example.vaccinationsystem.controller;

import com.example.vaccinationsystem.dto.AccountCreateRequest;
import com.example.vaccinationsystem.dto.AccountInfoDTO;
import com.example.vaccinationsystem.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountInfoDTO> listAccounts() {
        return accountService.getAllAccounts();
    }

    @PostMapping
    public String create(@RequestBody AccountCreateRequest request) {
        return accountService.createAccount(request);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable String id, @RequestBody AccountCreateRequest request) {
        accountService.updateAccount(id, request);
    }

    @DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> delete(@PathVariable String id) {
        try {
            accountService.deleteAccount(id);
            return org.springframework.http.ResponseEntity.ok(java.util.Collections.singletonMap("message", "Deleted"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(400)
                    .body(java.util.Collections.singletonMap("message", "Không thể xóa: Nhân sự này có ràng buộc dữ liệu (tiêm chủng/thanh toán) chưa thể gỡ bỏ."));
        }
    }

    @GetMapping("/search")
    public List<AccountInfoDTO> searchStaff(
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return accountService.searchStaff(role, keyword);
    }
}
