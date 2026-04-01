package com.example.vaccinationsystem.dao.controller;

import com.example.vaccinationsystem.dto.CustomerDTO;
import com.example.vaccinationsystem.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerDTO> getAll() {
        return customerService.getAll();
    }

    @GetMapping("/search")
    public List<CustomerDTO> searchByName(@RequestParam("name") String name) {
        return customerService.searchByName(name);
    }
}
