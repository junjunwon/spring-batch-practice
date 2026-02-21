package com.dev.batchpractice.domain.repository;

import com.dev.batchpractice.domain.entity.BatchInput;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchInputRepository extends JpaRepository<BatchInput, Long> {

	@Query("SELECT b FROM BatchInput b WHERE b.processed = false ORDER BY b.id")
	Page<BatchInput> findUnprocessedInputs(Pageable pageable);

	long countByProcessedFalse();
}
