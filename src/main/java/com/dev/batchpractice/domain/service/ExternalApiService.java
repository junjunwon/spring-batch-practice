package com.dev.batchpractice.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalApiService {

	/**
	 * 외부 API 호출을 시뮬레이션합니다.
	 * 실제 응답 시간 150ms를 고정으로 시뮬레이션합니다.
	 *
	 * @param inputData 처리할 입력 데이터
	 * @return API 응답 데이터
	 */
	public String callExternalApi(String inputData) {
		try {
			// 150ms 응답 시간 시뮬레이션
			Thread.sleep(150);
			
			// 실제 API 호출 로직이 여기에 들어갑니다
			// 예: RestTemplate, WebClient 등을 사용한 HTTP 호출
			
			log.info("External API called for data: {}", inputData);
			
			// 시뮬레이션된 응답 반환
			return "Processed: " + inputData;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("API call interrupted", e);
		}
	}

	/**
	 * 외부 API 호출 결과를 처리된 데이터로 변환합니다.
	 *
	 * @param apiResponse API 응답
	 * @return 처리된 데이터
	 */
	public String processApiResponse(String apiResponse) {
		// API 응답을 처리하여 최종 데이터로 변환
		return apiResponse;
	}
}
