package com.beehat.repository;

import com.beehat.entity.Employee;
import com.beehat.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepo extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByStatusAndInvoiceStatusOrderByUpdatedDateDesc(Byte status, Byte invoiceStatus);
    List<Invoice> findByStatusAndInvoiceStatus(Byte status, Byte invoiceStatus);
    List<Invoice> findByCustomerId(Integer id);
    List<Invoice> findByStatusAndInvoiceStatusAndEmployee(Byte status, Byte invoiceStatus, Employee e);
    List<Invoice> findByInvoiceStatus(Byte invoiceStatus, Sort sort);
    List<Invoice> findByStatus(Byte status);
    Invoice findByInvoiceTrackingNumber(String trackNumber);
    @Query("SELECT I FROM Invoice I WHERE I.voucher.id = ?1 AND (I.invoiceStatus = 0 OR I.invoiceStatus = 1) AND (I.status = 2 OR I.status = 8)")
    List<Invoice> findInvoiceByVoucher(Integer voucherId);
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN i.customer c " +
            "WHERE (:searchTerm IS NULL OR " +
            "       LOWER(i.invoiceTrackingNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "       LOWER(i.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "       LOWER(c.fullname) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:invoiceType IS NULL OR i.invoiceStatus = :invoiceType) " +
            "AND (:startDate IS NULL OR i.createdDate >= :startDate) " +
            "AND (:endDate IS NULL OR i.createdDate <= :endDate)")
    Page<Invoice> searchInvoices(@Param("searchTerm") String searchTerm,
                                 @Param("invoiceType") Byte invoiceType,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate, Pageable pageable);
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status AND i.invoiceStatus = :invoiceStatus")
    int countByStatusAndInvoiceStatus(byte status, byte invoiceStatus);
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN i.customer c " +
            "WHERE (:searchTerm IS NULL OR " +
            "       LOWER(i.invoiceTrackingNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "       LOWER(i.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "       LOWER(c.fullname) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:invoiceType IS NULL OR i.invoiceStatus = :invoiceType) " +
            "AND (:startDate IS NULL OR i.createdDate >= :startDate) " +
            "AND (:endDate IS NULL OR i.createdDate <= :endDate)"+
            "AND i.invoiceStatus=1")
    Page<Invoice> searchInvoices1(@Param("searchTerm") String searchTerm,
                                 @Param("invoiceType") Byte invoiceType,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate, Pageable pageable);
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.invoiceStatus = 1 AND i.status = 3")
    Integer countOnlineOrdersByStatus();
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.invoiceStatus = 0 AND i.status = 2 AND i.employee.id = :employeeId")
    Integer countInStorePaidOrders(@Param("employeeId") Integer employeeId);
}
