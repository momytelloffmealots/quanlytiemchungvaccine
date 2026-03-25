package com.example.vaccinationsystem.controller;

import com.example.vaccinationsystem.dto.VaccineDTO;
import com.example.vaccinationsystem.dto.VaccineExpiringDTO;
import com.example.vaccinationsystem.dto.VaccineTypeDTO;
import com.example.vaccinationsystem.service.VaccineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
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
}

