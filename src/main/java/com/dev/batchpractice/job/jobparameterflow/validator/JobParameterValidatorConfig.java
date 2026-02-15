package com.dev.batchpractice.job.jobparameterflow.validator;

import org.springframework.batch.core.job.parameters.CompositeJobParametersValidator;
import org.springframework.batch.core.job.parameters.DefaultJobParametersValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ===== Job 실행 파라미터 규칙 =====
 * <p>
 * 1️⃣ 공통 필수 파라미터
 * - name : 사용자 식별 값 (항상 필요)
 * - mode : 실행 모드 (FULL / DELTA / FILE 중 하나)
 * <p>
 * 2️⃣ FULL 모드
 * - 전체 데이터 처리
 * - 추가 파라미터 없음
 * <p>
 * 실행 예시:
 * --job.name=jobParameterFlowTaskletJob mode=FULL name=jihyun
 * <p>
 * <p>
 * 3️⃣ DELTA 모드
 * - 기간 기반 증분 처리
 * - startDate, endDate 필수
 * <p>
 * 실행 예시:
 * --job.name=jobParameterFlowTaskletJob mode=DELTA name=jihyun startDate=20240101 endDate=20240131
 * <p>
 * <p>
 * 4️⃣ FILE 모드
 * - 파일 기반 처리
 * - fileName 필수
 * <p>
 * 실행 예시:
 * --job.name=jobParameterFlowTaskletJob mode=FILE name=jihyun fileName=data.csv
 * <p>
 * <p>
 * ===== 검증 규칙 =====
 * - mode 값은 FULL, DELTA, FILE 중 하나여야 함
 * - DELTA 모드 → startDate, endDate 없으면 실행 실패
 * - FILE 모드 → fileName 없으면 실행 실패
 */
@Configuration
public class JobParameterValidatorConfig {

    @Bean
    public CompositeJobParametersValidator parametersValidator() {

        DefaultJobParametersValidator requiredValidator =
                new DefaultJobParametersValidator(
                        new String[]{"name", "mode"},
                        new String[]{"startDate", "endDate", "fileName"}
                );

        requiredValidator.afterPropertiesSet();

        CompositeJobParametersValidator composite = new CompositeJobParametersValidator();

        composite.setValidators(List.of(
                requiredValidator,
                new ModeValidator(),
                new DeltaModeValidator(),
                new FileModeValidator()
        ));

        return composite;
    }
}
