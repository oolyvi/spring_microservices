package com.atm.cashservice;

import jakarta.persistence.*;

@Entity
@Table(name = "cash")
public class Cash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "atm_id", nullable = false)
    private Long atmId;

    @Column(nullable = false)
    private Integer denomination; // e.g., 1, 5, 10, 20, 50, 100, 200, 500

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "low_threshold", nullable = false)
    private Integer lowThreshold = 10;

    public Cash() {
    }

    public Cash(Long atmId, Integer denomination, Integer quantity, Integer lowThreshold) {
        this.atmId = atmId;
        this.denomination = denomination;
        this.quantity = quantity;
        this.lowThreshold = lowThreshold;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAtmId() {
        return atmId;
    }

    public void setAtmId(Long atmId) {
        this.atmId = atmId;
    }

    public Integer getDenomination() {
        return denomination;
    }

    public void setDenomination(Integer denomination) {
        this.denomination = denomination;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getLowThreshold() {
        return lowThreshold;
    }

    public void setLowThreshold(Integer lowThreshold) {
        this.lowThreshold = lowThreshold;
    }
}