package com.beehat.repository;

import com.beehat.entity.Employee;
import com.beehat.entity.Invoice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepo extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByStatusAndInvoiceStatus(Byte status, Byte invoiceStatus);
    List<Invoice> findByCustomerId(Integer id);
    List<Invoice> findByStatusAndInvoiceStatusAndEmployee(Byte status, Byte invoiceStatus, Employee e);
    List<Invoice> findByInvoiceStatus(Byte invoiceStatus, Sort sort);
    Invoice findByInvoiceTrackingNumber(String trackNumber);
}
