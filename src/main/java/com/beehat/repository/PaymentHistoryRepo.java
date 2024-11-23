package com.beehat.repository;

import com.beehat.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryRepo extends JpaRepository<PaymentHistory, Integer> {
}
