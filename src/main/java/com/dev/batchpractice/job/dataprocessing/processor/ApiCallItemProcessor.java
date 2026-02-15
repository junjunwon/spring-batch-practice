package com.dev.batchpractice.job.dataprocessing.processor;

import com.dev.batchpractice.domain.entity.BatchInput;
import com.dev.batchpractice.domain.entity.BatchOutput;
import com.dev.batchpractice.domain.service.ExternalApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiCallItemProcessor implements ItemProcessor<BatchInput, BatchOutput> {

	private final ExternalApiService externalApiService;

	@Override
	public BatchOutput process(BatchInput item) {
		log.info("Processing item: id={}, name={}", item.getId(), item.getName());

		// 외부 API 호출 (150ms 응답 시간)
		String apiResponse = externalApiService.callExternalApi(item.getData());

		// API 응답 처리
		String processedData = externalApiService.processApiResponse(apiResponse);

		// BatchOutput 엔티티 생성
		BatchOutput output = BatchOutput.builder()
				.inputId(item.getId())
				.name(item.getName())
				.originalData(item.getData())
				.processedData(processedData)
				.apiResponse(apiResponse)
				.build();

		log.info("Processed item: inputId={}, processedData={}", item.getId(), processedData);

		return output;
	}
}
