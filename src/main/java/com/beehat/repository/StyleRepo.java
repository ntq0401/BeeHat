package com.beehat.repository;

import com.beehat.entity.Style;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StyleRepo extends JpaRepository<Style, Integer> {
}
