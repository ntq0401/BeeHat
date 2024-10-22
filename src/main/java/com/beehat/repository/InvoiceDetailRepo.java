package com.beehat.repository;

import com.beehat.entity.InvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceDetailRepo extends JpaRepository<InvoiceDetail, Integer> {
    List<InvoiceDetail> findByInvoiceId(Integer invoiceId);
}
