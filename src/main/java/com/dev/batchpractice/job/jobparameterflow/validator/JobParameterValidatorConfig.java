package com.dev.batchpractice.job.jobparameterflow.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.parameters.CompositeJobParametersValidator;
import org.springframework.batch.core.job.parameters.DefaultJobParametersValidator;
import org.springframework.batch.core.job.parameters.JobParametersValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ===== Job 실행 파라미터 규칙 =====
 * <p>
 * 1. 공통 필수 파라미터
 * - name : 사용자 식별 값 (항상 필요)
 * - mode : 실행 모드 (FULL / DELTA / FILE 중 하나)
 * <p>
 * 2. FULL 모드
 * - 전체 데이터 처리
 * - 추가 파라미터 없음
 * <p>
 * 실행 예시:
 * --job.name=jobParameterFlowTaskletJob mode=FULL name=jihyun
 * <p>
 * <p>
 * 3. DELTA 모드
 * - 기간 기반 증분 처리
 * - startDate, endDate 필수
 * <p>
 * 실행 예시:
 * --job.name=jobParameterFlowTaskletJob mode=DELTA name=jihyun startDate=20240101 endDate=20240131
 * <p>
 * <p>
 * 4. FILE 모드
 * - 파일 기반 처리
 * - fileName 필수
 * <p>
 * 실행 예시:
 * --job.name=jobParameterFlowTaskletJob mode=FILE name=jihyun fileName=data.csv
 * <p>
 * <p>
 * ===== 검증 규칙 =====
 * <p>
 * - mode 값은 FULL, DELTA, FILE 중 하나여야 함
 * <p>
 * - DELTA 모드 → startDate, endDate 없으면 실행 실패
 * <p>
 * - FILE 모드 → fileName 없으면 실행 실패
 */
@Slf4j
@Configuration
public class JobParameterValidatorConfig {

//    // 1번 해결방법 - 생성자 직접 작성
//    private final List<JobParametersValidator> validators;
//
//    public JobParameterValidatorConfig(
//            @Qualifier("jobParameter") List<JobParametersValidator> validators) {
//        this.validators = validators;
//    }

    /**
     * 특정 Validator 만 선택하고 싶은 경우, <p>
     * 클래스에 @RequiredArgsConstructor + 필드에 @Qualifier("jobParameter")를 붙이면
     * 의도대로 동작하지 않는다.
     *
     * <p><b>⚠️ 왜 @Qualifier가 있는데도 모든 Validator가 주입될까?</b></p>
     *
     * <p><b>원인</b></p>
     * <p>
     * - 이 클래스는 @RequiredArgsConstructor(Lombok)를 사용해 생성자 주입을 자동 생성한다.<br>
     * - 하지만 필드에 붙인 @Qualifier는 생성자 파라미터로 전달되지 않는다.
     * </p>
     *
     * <p><b>Lombok이 실제로 생성하는 생성자 형태</b></p>
     * <pre>
     * public JobParameterValidatorConfig(List&lt;JobParametersValidator&gt; validators)
     * </pre>
     *
     * <p><b>즉 Spring 입장에서는</b></p>
     * <p>
     * - @Qualifier 정보 없음<br>
     * - 타입(JobParametersValidator)만 보고 Bean 주입<br>
     * - 결과 → 해당 타입의 모든 Bean이 List에 주입됨
     * </p>
     *
     * <p><b>핵심 규칙</b></p>
     * <p>
     * ✔ @Qualifier는 "생성자 파라미터" 또는 "@Bean 메서드 파라미터"에 있어야 동작한다.<br>
     * ✔ 필드에만 붙이면 Lombok 생성자 주입에서는 무시된다.
     * </p>
     *
     * <p><b>해결 방법</b></p>
     *
     * <p><b>1. 생성자를 직접 작성해서 파라미터에 @Qualifier 적용</b></p>
     * <pre>
     * public JobParameterValidatorConfig(
     *     @Qualifier("jobParameter") List&lt;JobParametersValidator&gt; validators
     * ) {
     *     this.validators = validators;
     * }
     * </pre>
     *
     * <p><b>2. @Bean 메서드 파라미터에서 직접 주입 (권장)</b></p>
     * <pre>
     * @Bean
     * public CompositeJobParametersValidator parametersValidator(
     *     @Qualifier("jobParameter") List&lt;JobParametersValidator&gt; validators
     * ) {
     *     ...
     * }
     * </pre>
     *
     * <p><b>정리</b></p>
     * <p>
     * - @Qualifier는 "주입 지점"에 있어야 필터가 적용된다.<br>
     * - 필드 + Lombok 생성자 주입 조합에서는 동작하지 않는다.
     * </p>
     */

    @Bean
    public CompositeJobParametersValidator parametersValidator(
            @Qualifier("jobParameter") List<JobParametersValidator> validators
    ) {
        log.info("## validators = {}", validators);

        DefaultJobParametersValidator requiredValidator =
                new DefaultJobParametersValidator(
                        new String[]{"name", "mode"},
                        new String[]{"startDate", "endDate", "fileName", "currentDate"}
                );

        requiredValidator.afterPropertiesSet();

        CompositeJobParametersValidator composite = new CompositeJobParametersValidator();

        // 공통 타입인 JobParametersValidator 인터페이스를 구현한 모든 검증기를 자동으로 주입받아 설정
        composite.setValidators(validators);

        return composite;
    }
}
