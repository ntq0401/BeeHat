package com.beehat.repository;

import com.beehat.entity.Customer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepo extends JpaRepository<Customer, Integer> {
    @Query("SELECT e FROM Customer e WHERE e.id = :id1")
    Customer findeById(@Param("id1") Integer id);
    @Transactional
    @Query("SELECT e FROM Customer e WHERE e.username = :username")
    Customer findByUsername(@Param("username") String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    @Query("SELECT c.fullname FROM Customer c WHERE LOWER(c.fullname) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<String> findCustomerNamesByQuery(@Param("query") String query);
}
