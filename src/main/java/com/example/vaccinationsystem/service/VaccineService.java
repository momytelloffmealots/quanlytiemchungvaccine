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
        // Use manual ID provided by user
        String vaccineId = dto.getVaccineId() != null ? dto.getVaccineId().trim() : "";
        if (vaccineId.isEmpty()) {
            throw new IllegalArgumentException("Mã vaccine không được để trống");
        }
        
        // Check if ID already exists
        if (vaccineDao.getVaccineName(vaccineId).isPresent()) {
            throw new IllegalArgumentException("Mã vaccine '" + vaccineId + "' đã tồn tại trong hệ thống");
        }
        
        // Handle Vaccine Type (check if exists, if not create)
        String typeId = getOrCreateTypeId(dto.getVaccineTypeName());
        dto.setVaccineTypeId(typeId);
        
        // Final fallback for inventoryManagerId if frontend failed to provide one
        if (dto.getInventoryManagerId() == null || dto.getInventoryManagerId().trim().isEmpty()) {
            String fallbackId = vaccineDao.getAnyInventoryManagerId();
            dto.setInventoryManagerId(fallbackId);
        }
        
        vaccineDao.insertVaccine(vaccineId, dto);
        return vaccineId;
    }

    private String getOrCreateTypeId(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại vaccine không được để trống");
        }
        
        String nameNormalized = typeName.trim();
        // Check for existing type by name
        return vaccineDao.findVaccineTypeByName(nameNormalized)
                .map(com.example.vaccinationsystem.dto.VaccineTypeDTO::getVaccineTypeId)
                .orElseGet(() -> {
                    // If not found, generate a new ID and insert
                    String lastId = vaccineDao.getLatestVaccineTypeId();
                    String nextId = generateNextId(lastId, "VT");
                    
                    // Safety check: ensure this nextId is not somehow already in use 
                    // (prevents 500 if IDs in DB were manually messed up)
                    vaccineDao.insertVaccineType(nextId, nameNormalized);
                    return nextId;
                });
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

