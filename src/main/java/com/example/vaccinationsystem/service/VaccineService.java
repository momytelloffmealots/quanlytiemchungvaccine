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

    public String createVaccine(VaccineDTO dto) {
        String lastId = vaccineDao.getLatestVaccineId();
        String nextId = generateNextId(lastId, "VC");
        vaccineDao.insertVaccine(nextId, dto);
        return nextId;
    }

    private String generateNextId(String currentId, String prefix) {
        StringBuilder numStr = new StringBuilder();
        for (int i = currentId.length() - 1; i >= 0; i--) {
            char c = currentId.charAt(i);
            if (Character.isDigit(c)) numStr.insert(0, c);
            else break;
        }
        int nextNum = 1;
        if (numStr.length() > 0) {
            nextNum = Integer.parseInt(numStr.toString()) + 1;
        }
        return String.format("%s%03d", prefix, nextNum);
    }
}

