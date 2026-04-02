package com.example.vaccinationsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VaccineDTO {
    private String vaccineId;
    private String name;
    private String manufacturer;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate productionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    private String lot;
    private int quantityAvailable;
    private BigDecimal price;

    private String vaccineTypeId;
    private String vaccineTypeName;
    private Integer daysLeft;

    public String getVaccineId() {
        return vaccineId;
    }

    public void setVaccineId(String vaccineId) {
        this.vaccineId = vaccineId;
    }

    public Integer getDaysLeft() {
        return daysLeft;
    }

    public void setDaysLeft(Integer daysLeft) {
        this.daysLeft = daysLeft;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public LocalDate getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getVaccineTypeId() {
        return vaccineTypeId;
    }

    public void setVaccineTypeId(String vaccineTypeId) {
        this.vaccineTypeId = vaccineTypeId;
    }

    public String getVaccineTypeName() {
        return vaccineTypeName;
    }

    public void setVaccineTypeName(String vaccineTypeName) {
        this.vaccineTypeName = vaccineTypeName;
    }
}

