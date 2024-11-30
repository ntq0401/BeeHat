package com.beehat.repository;

import com.beehat.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface VoucherRepo extends JpaRepository<Voucher, Integer> {
    List<Voucher> findByStatus(byte status);
    List<Voucher> findByStatusAndEndDate(byte status, LocalDateTime endDate);
    Voucher findByCode(String code);
}
