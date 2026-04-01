package com.example.vaccinationsystem.service;

import com.example.vaccinationsystem.dao.VaccinationFormDao;
import com.example.vaccinationsystem.dao.VaccineDao;
import com.example.vaccinationsystem.dto.VaccinationFormCreateRequest;
import com.example.vaccinationsystem.dto.VaccinationFormInfoDTO;
import com.example.vaccinationsystem.dto.VaccinationFormVaccineDTO;
import com.example.vaccinationsystem.util.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VaccinationFormService {
    private final VaccinationFormDao formDao;
    private final VaccineDao vaccineDao;

    public VaccinationFormService(VaccinationFormDao formDao, VaccineDao vaccineDao) {
        this.formDao = formDao;
        this.vaccineDao = vaccineDao;
    }

    @Transactional
    public String createVaccinationForm(VaccinationFormCreateRequest req) {
        if (req.getDetails() == null || req.getDetails().isEmpty()) {
            throw new IllegalArgumentException("details is required");
        }

        String formId = (req.getVaccinationFormId() == null || req.getVaccinationFormId().isBlank())
                ? IdGenerator.nextPrefixedId("PT", formDao.getLastestFormId())
                : req.getVaccinationFormId().trim();

        if (formDao.existsById(formId)) {
            throw new IllegalArgumentException("Vaccination form already exists: " + formId);
        }

        // Reserve stock first to fail fast (transaction will roll back).
        for (var det : req.getDetails()) {
            int updated = formDao.decrementVaccineStock(det.getVaccineId(), det.getCnt());
            if (updated <= 0) {
                int available = vaccineDao.getQuantityAvailable(det.getVaccineId());
                throw new IllegalArgumentException("Not enough stock for vaccineId=" + det.getVaccineId()
                        + ". Available=" + available + ", required=" + det.getCnt());
            }
        }

        formDao.insertFormRow(formId, req);
        formDao.insertFormDetails(formId, req.getDetails());
        return formId;
    }

    public List<VaccinationFormInfoDTO> getAllFormsInfo() {
        return formDao.getAllFormsInfo();
    }

    public List<VaccinationFormVaccineDTO> getVaccinesFromForm(String formId) {
        if (!formDao.existsById(formId)) {
            throw new IllegalArgumentException("Vaccination form not found: " + formId);
        }
        return formDao.getVaccinesFromForm(formId);
    }

    public Optional<String> getCustomerNameFromForm(String formId) {
        return formDao.getCustomerNameFromForm(formId);
    }

    public boolean hasBillForForm(String formId) {
        return formDao.hasBillForForm(formId);
    }

    @Transactional
    public void deleteForm(String formId) {
        if (!formDao.existsById(formId)) {
            throw new IllegalArgumentException("Vaccination form not found: " + formId);
        }
        // If there is bill, you can decide to block; for now we just block to preserve data integrity.
        if (formDao.hasBillForForm(formId)) {
            throw new IllegalArgumentException("Cannot delete: form already has a bill: " + formId);
        }
        formDao.deleteForm(formId);
    }

    public List<VaccinationFormInfoDTO> searchForms(java.time.LocalDate retentionDate, Integer month) {
        return formDao.searchForms(retentionDate, month);
    }
}

