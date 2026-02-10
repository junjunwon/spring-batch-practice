package com.dev.batchpractice.domain.repository;

import com.dev.batchpractice.domain.entity.BatchOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchOutputRepository extends JpaRepository<BatchOutput, Long> {
}
