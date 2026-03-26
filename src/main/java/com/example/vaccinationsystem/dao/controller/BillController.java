package com.example.vaccinationsystem.dao.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaccinationsystem.dto.BillCreateRequest;
import com.example.vaccinationsystem.dto.BillInfoDTO;
import com.example.vaccinationsystem.service.BillService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BillController {
    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping("/bills")
    public List<BillInfoDTO> getAllBills() {
        return billService.getAllBillInfo();
    }

    @PostMapping(value = "/bills", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String createBill(@Valid @RequestBody BillCreateRequest req) {
        return billService.createBill(req);
    }
}

