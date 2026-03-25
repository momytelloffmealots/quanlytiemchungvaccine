package com.example.vaccinationsystem.controller;

import com.example.vaccinationsystem.dto.VaccinationFormCreateRequest;
import com.example.vaccinationsystem.dto.VaccinationFormInfoDTO;
import com.example.vaccinationsystem.dto.VaccinationFormVaccineDTO;
import com.example.vaccinationsystem.service.VaccinationFormService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class VaccinationFormController {
    private final VaccinationFormService formService;

    public VaccinationFormController(VaccinationFormService formService) {
        this.formService = formService;
    }

    @GetMapping("/vaccination-forms")
    public List<VaccinationFormInfoDTO> getAllForms() {
        return formService.getAllFormsInfo();
    }

    @PostMapping(value = "/vaccination-forms", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String createForm(@Valid @RequestBody VaccinationFormCreateRequest request) {
        return formService.createVaccinationForm(request);
    }

    @GetMapping("/vaccination-forms/{id}/vaccines")
    public List<VaccinationFormVaccineDTO> getVaccinesFromForm(@PathVariable("id") String id) {
        return formService.getVaccinesFromForm(id);
    }

    @GetMapping("/vaccination-forms/{id}/customer-name")
    public Optional<String> getCustomerNameFromForm(@PathVariable("id") String id) {
        return formService.getCustomerNameFromForm(id);
    }

    @GetMapping("/vaccination-forms/{id}/payment-status")
    public boolean hasBillForForm(@PathVariable("id") String id) {
        return formService.hasBillForForm(id);
    }

    @DeleteMapping("/vaccination-forms/{id}")
    public void deleteForm(@PathVariable("id") String id) {
        formService.deleteForm(id);
    }
}

