package com.atm.cashservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashRepository extends JpaRepository<Cash, Long> {
    List<Cash> findByAtmIdOrderByDenominationDesc(Long atmId);
}