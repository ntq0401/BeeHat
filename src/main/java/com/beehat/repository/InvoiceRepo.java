package com.beehat.repository;

import com.beehat.entity.Employee;
import com.beehat.entity.Invoice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    @Query("SELECT I FROM Invoice I WHERE I.voucher.id = ?1 AND (I.invoiceStatus = 0 OR I.invoiceStatus = 1) AND (I.status = 2 OR I.status = 8)")
    List<Invoice> findInvoiceByVoucher(Integer voucherId);

}
