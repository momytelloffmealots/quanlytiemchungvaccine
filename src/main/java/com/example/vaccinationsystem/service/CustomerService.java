package com.example.vaccinationsystem.service;

import com.example.vaccinationsystem.dao.CustomerDao;
import com.example.vaccinationsystem.dto.CustomerDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerDao customerDao;

    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public List<CustomerDTO> getAll() {
        return customerDao.findAll();
    }

    public List<CustomerDTO> searchByName(String name) {
        return customerDao.searchByName(name);
    }
}
