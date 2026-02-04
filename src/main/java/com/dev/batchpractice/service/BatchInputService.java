package com.dev.batchpractice.service;

import com.dev.batchpractice.entity.BatchInput;
import com.dev.batchpractice.repository.BatchInputRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchInputService {

	private final BatchInputRepository batchInputRepository;
	private static final int PAGE_SIZE = 1000;

	/**
	 * 처리되지 않은 데이터 조회용 JPQL 쿼리 문자열 반환
	 */
	public String getUnprocessedInputsQuery() {
		return "SELECT b FROM BatchInput b WHERE b.processed = false ORDER BY b.id";
	}

	/**
	 * 처리되지 않은 데이터를 페이징으로 조회
	 */
	public Page<BatchInput> findUnprocessedInputs(int page) {
		Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
		return batchInputRepository.findUnprocessedInputs(pageable);
	}

	/**
	 * 처리되지 않은 데이터 개수 조회
	 */
	public long countUnprocessedInputs() {
		return batchInputRepository.countByProcessedFalse();
	}
}
