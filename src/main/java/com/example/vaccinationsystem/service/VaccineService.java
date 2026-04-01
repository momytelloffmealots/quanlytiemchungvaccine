package com.example.vaccinationsystem.service;

import com.example.vaccinationsystem.dao.VaccineDao;
import com.example.vaccinationsystem.dto.VaccineDTO;
import com.example.vaccinationsystem.dto.VaccineExpiringDTO;
import com.example.vaccinationsystem.dto.VaccineTypeDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class VaccineService {
    private final VaccineDao vaccineDao;

    public VaccineService(VaccineDao vaccineDao) {
        this.vaccineDao = vaccineDao;
    }

    public List<VaccineTypeDTO> getAllVaccineTypes() {
        return vaccineDao.findAllVaccineTypes();
    }

    public List<VaccineDTO> getAllVaccines() {
        return vaccineDao.findAllVaccine();
    }

    public List<VaccineExpiringDTO> getAllVaccineExpiringInDays(int days) {
        if (days <= 0) throw new IllegalArgumentException("days must be > 0");
        return vaccineDao.findExpiringInDays(days);
    }

    public List<VaccineDTO> searchVaccine(String keyword) {
        if (keyword == null) keyword = "";
        return vaccineDao.searchVaccine(keyword.trim());
    }

    public List<VaccineExpiringDTO> searchVaccineExpiring(String keyword, int days) {
        if (days <= 0) throw new IllegalArgumentException("days must be > 0");
        return vaccineDao.searchExpiringVaccine(keyword == null ? "" : keyword.trim(), days);
    }

    public List<VaccineDTO> searchAdvanced(String type, Integer maxQty, String lot, LocalDate start, LocalDate end) {
        return vaccineDao.searchAdvanced(type, maxQty, lot, start, end);
    }
}

