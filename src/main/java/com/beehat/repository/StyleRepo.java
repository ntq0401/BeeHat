package com.beehat.repository;

import com.beehat.entity.Size;
import com.beehat.entity.Style;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StyleRepo extends JpaRepository<Style, Integer> {
    List<Style> findByStatus(Byte status);
    List<Style> findByNameContainingIgnoreCase(String name);

}
