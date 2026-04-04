package com.example.vaccinationsystem.dao.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaccinationsystem.dto.VaccineDTO;
import com.example.vaccinationsystem.dto.VaccineExpiringDTO;
import com.example.vaccinationsystem.dto.VaccineTypeDTO;
import com.example.vaccinationsystem.service.VaccineService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class VaccineController {
    private final VaccineService vaccineService;

    public VaccineController(VaccineService vaccineService) {
        this.vaccineService = vaccineService;
    }

    @GetMapping("/vaccine-types")
    public List<VaccineTypeDTO> getVaccineTypes() {
        return vaccineService.getAllVaccineTypes();
    }

    @GetMapping("/vaccines")
    public List<VaccineDTO> getAllVaccines() {
        return vaccineService.getAllVaccines();
    }

    @GetMapping("/vaccines/expiring")
    public List<VaccineExpiringDTO> getVaccinesExpiringInDays(@RequestParam("days") int days) {
        return vaccineService.getAllVaccineExpiringInDays(days);
    }

    @GetMapping("/vaccines/search")
    public List<VaccineDTO> searchVaccines(@RequestParam("keyword") String keyword) {
        return vaccineService.searchVaccine(keyword);
    }

    @GetMapping("/vaccines/search/expiring")
    public List<VaccineExpiringDTO> searchVaccinesExpiring(@RequestParam("keyword") String keyword,
                                                              @RequestParam("days") int days) {
        return vaccineService.searchVaccineExpiring(keyword, days);
    }
    @GetMapping("/vaccines/search-advanced")
    public List<com.example.vaccinationsystem.dto.VaccineDTO> searchAdvanced(
            @RequestParam(value = "typeName", required = false) String typeName,
            @RequestParam(value = "maxQuantity", required = false) Integer maxQuantity,
            @RequestParam(value = "lot", required = false) String lot,
            @RequestParam(value = "startDate", required = false) java.time.LocalDate startDate,
            @RequestParam(value = "endDate", required = false) java.time.LocalDate endDate) {
        return vaccineService.searchAdvanced(typeName, maxQuantity, lot, startDate, endDate);
    }

    @GetMapping("/vaccines/{id}/price")
    public java.math.BigDecimal getVaccinePrice(@org.springframework.web.bind.annotation.PathVariable("id") String id) {
        return vaccineService.getVaccinePrice(id);
    }

    @PostMapping("/vaccines")
    public String createVaccine(@RequestBody VaccineDTO dto) {
        return vaccineService.createVaccine(dto);
    }
}

