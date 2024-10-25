package com.beehat.repository;

import com.beehat.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepo extends JpaRepository<Customer, Integer> {
    @Query("SELECT c.fullname FROM Customer c WHERE LOWER(c.fullname) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<String> findCustomerNamesByQuery(@Param("query") String query);
}
