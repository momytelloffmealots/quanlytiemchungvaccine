package com.example.vaccinationsystem.service;

import com.example.vaccinationsystem.dao.BillDao;
import com.example.vaccinationsystem.dao.VaccinationFormDao;
import com.example.vaccinationsystem.dto.BillCreateRequest;
import com.example.vaccinationsystem.dto.BillInfoDTO;
import com.example.vaccinationsystem.util.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BillService {
    private final BillDao billDao;
    private final VaccinationFormDao formDao;

    public BillService(BillDao billDao, VaccinationFormDao formDao) {
        this.billDao = billDao;
        this.formDao = formDao;
    }

    @Transactional
    public String createBill(BillCreateRequest req) {
        if (!formDao.existsById(req.getVaccinationFormId())) {
            throw new IllegalArgumentException("Vaccination form not found: " + req.getVaccinationFormId());
        }
        if (billDao.hasBillForForm(req.getVaccinationFormId())) {
            throw new IllegalArgumentException("Bill already exists for form: " + req.getVaccinationFormId());
        }

        String billId = (req.getBillId() == null || req.getBillId().isBlank())
                ? IdGenerator.nextPrefixedId("HD", billDao.getLastestBillId())
                : req.getBillId().trim();

        BigDecimal total = req.getTotalAmount();
        if (total == null) {
            total = billDao.computeTotalAmountFromDetails(req.getVaccinationFormId());
            req.setTotalAmount(total);
        }

        billDao.insertBill(billId, req);
        return billId;
    }

    public List<BillInfoDTO> getAllBillInfo() {
        return billDao.findAllBillInfo();
    }

    public BigDecimal getOriginalTotal(String formId) {
        return billDao.computeTotalAmountFromDetails(formId);
    }
}

