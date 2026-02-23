package com.dev.batchpractice.job.dataprocessing;

import com.dev.batchpractice.common.listener.BatchPerformanceListener;
import com.dev.batchpractice.domain.entity.BatchInput;
import com.dev.batchpractice.domain.entity.BatchOutput;
import com.dev.batchpractice.job.dataprocessing.processor.ApiCallItemProcessor;
import com.dev.batchpractice.job.dataprocessing.tasklet.DataInitializationTasklet;
import com.dev.batchpractice.job.dataprocessing.tasklet.FailStepTasklet;
import com.dev.batchpractice.job.dataprocessing.writer.BatchOutputWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.CompositeJobParametersValidator;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.parameters.JobParametersIncrementer;
import org.springframework.batch.core.job.parameters.JobParametersValidator;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.infrastructure.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.infrastructure.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.infrastructure.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Spring Batch 학습 예제가 포함된 Job 설정.
 * - JobParametersValidator, CompositeJobParametersValidator
 * - JobParametersIncrementer, DailyJobTimeStamper
 * - JobExecutionListener
 * - ExecutionContextPromotionListener
 * - Tasklet RepeatStatus.CONTINUABLE
 * - CallableTaskletAdapter
 * - CompletionPolicy (SimpleCompletionPolicy, TimeoutTerminationPolicy)
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchJobConfig {
	private static final int CHUNK_SIZE = 10;

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	private final DataInitializationTasklet dataInitializationTasklet;
	private final FailStepTasklet failStepTasklet;

	private final ApiCallItemProcessor apiCallItemProcessor;
	private final BatchOutputWriter batchOutputWriter;

	private final BatchPerformanceListener batchPerformanceListener;

	// ========== 1. JobParametersValidator, CompositeJobParametersValidator ==========
	/**
	 * 학습: 여러 검증기를 조합하는 CompositeJobParametersValidator.
	 * run.id가 있거나, name 파라미터가 있으면 통과.
	 */
	@Bean
	public CompositeJobParametersValidator compositeJobParametersValidator() {
		CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
		validator.setValidators(List.of(
				// run.id 또는 timestamp가 있으면 통과 (incrementer 사용 시)
				params -> {
					if (params != null && (params.getLong("run.id") != null || params.getLong("timestamp") != null)) {
						return;
					}
					throw new InvalidJobParametersException("run.id or timestamp parameter is required");
				},
				// name은 선택적 (simpleTaskletJob용)
				params -> {
					if (params != null && params.getString("name") != null && params.getString("name").isEmpty()) {
						throw new InvalidJobParametersException("name cannot be empty when provided");
					}
				}
		));
		return validator;
	}

	/**
	 * 학습: dataProcessingJob 전용 validator.
	 * incrementer가 run.id를 추가하므로, 파라미터가 null이면 통과 (incrementer가 처리).
	 */
	@Bean
	public JobParametersValidator dataProcessingJobValidator() {
		return parameters -> {
			if (parameters != null && parameters.getLong("run.id") == null
					&& parameters.getLong("timestamp") == null) {
				throw new InvalidJobParametersException("run.id or timestamp is required");
			}
		};
	}

	// ========== 2. JobParametersIncrementer (RunIdIncrementer, DailyJobTimeStamper) ==========
	/**
	 * 학습: RunIdIncrementer - 동일 잡을 여러 번 실행할 때 run.id를 자동 증가시켜
	 * JobInstanceAlreadyCompleteException 방지.
	 */
	@Bean
	public RunIdIncrementer runIdIncrementer() {
		return new RunIdIncrementer();
	}

	/**
	 * 학습: DailyJobTimeStamper - 날짜 단위로 잡을 실행할 때 사용.
	 * run.date 파라미터를 오늘 날짜로 설정.
	 */
	@Bean
	public JobParametersIncrementer dailyJobTimeStamper() {
		return parameters -> {
			String runDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
			return new JobParametersBuilder(parameters != null ? parameters : new JobParameters())
					.addString("run.date", runDate)
					.addLong("run.id", System.currentTimeMillis())
					.toJobParameters();
		};
	}

	// ========== 3. JobExecutionListener ==========
	/**
	 * 학습: afterJob은 잡의 완료 상태(SUCCESS/FAILED)와 관계없이 항상 호출됨.
	 * BatchPerformanceListener가 이미 구현되어 있음.
	 */

	// ========== 4. ExecutionContextPromotionListener ==========
	/**
	 * 학습: StepExecutionContext의 키를 JobExecutionContext로 승격.
	 * Step이 성공했을 때만 승격됨 (기본: COMPLETED).
	 */
	@Bean
	public ExecutionContextPromotionListener executionContextPromotionListener() {
		ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
		listener.setKeys(new String[]{"promotedKey", "initCount"});
		return listener;
	}

	/**
	 * 학습: Step에 데이터를 넣고, ExecutionContextPromotionListener로 Job 레벨로 승격하는 Tasklet.
	 */
	@Bean
	public Tasklet contextPromotionTasklet() {
		return (contribution, chunkContext) -> {
			ExecutionContext stepContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
			stepContext.put("promotedKey", "value-from-step1");
			stepContext.put("initCount", 100);
			log.info("Step ExecutionContext에 promotedKey, initCount 저장 (승격 대상)");
			return RepeatStatus.FINISHED;
		};
	}

	// ========== 5. BATCH_JOB_EXECUTION_CONTEXT ==========
	// 학습: SERIALIZED_CONTEXT 컬럼은 잡이 실행 중이거나 실패한 경우에만 채워짐.

	// ========== 6. Tasklet RepeatStatus.CONTINUABLE ==========
	/**
	 * 학습: CONTINUABLE 반환 시 해당 Tasklet을 다시 실행.
	 * maxCount 도달 시 FINISHED 반환.
	 */
	@Bean
	public Tasklet continuableTasklet() {
		return new Tasklet() {
			private int count = 0;
			private static final int MAX_COUNT = 3;

			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
				count++;
				log.info("CONTINUABLE Tasklet 실행 {}회/{}회", count, MAX_COUNT);
				return count >= MAX_COUNT ? RepeatStatus.FINISHED : RepeatStatus.CONTINUABLE;
			}
		};
	}

	// ========== 7. CallableTaskletAdapter ==========
	/**
	 * 학습: Callable을 다른 스레드에서 실행. 스텝 스레드와 별개이나 병렬은 아님.
	 * Callable이 RepeatStatus를 반환할 때까지 스텝은 완료로 간주되지 않음.
	 */
	@Bean
	public CallableTaskletAdapter callableTaskletAdapter() {
		Callable<RepeatStatus> callable = () -> {
			log.info("CallableTaskletAdapter: 별도 스레드에서 실행됨");
			Thread.sleep(500);
			return RepeatStatus.FINISHED;
		};
		return new CallableTaskletAdapter(callable);
	}

	// ========== 8. CompletionPolicy (SimpleCompletionPolicy, TimeoutTerminationPolicy) ==========
	/**
	 * 학습: 정적 청크 크기 - chunk(10).
	 */
	/**
	 * 학습: SimpleCompletionPolicy - 고정 개수 기반 청크 완료.
	 */
	@Bean
	public SimpleCompletionPolicy simpleCompletionPolicy() {
		return new SimpleCompletionPolicy(5);
	}

	/**
	 * 학습: TimeoutTerminationPolicy - 시간(ms) 기반 청크 종료.
	 */
	@Bean
	public TimeoutTerminationPolicy timeoutTerminationPolicy() {
		return new TimeoutTerminationPolicy(3000);
	}

	/**
	 * 학습: CompositeCompletionPolicy - 여러 정책 조합 (5개 또는 3초 중 먼저 도달).
	 */
	@Bean
	public CompositeCompletionPolicy compositeCompletionPolicy() {
		CompositeCompletionPolicy policy = new CompositeCompletionPolicy();
		policy.setPolicies(new org.springframework.batch.infrastructure.repeat.CompletionPolicy[]{
				new SimpleCompletionPolicy(5),
				new TimeoutTerminationPolicy(3000)
		});
		return policy;
	}

	// ========== Job & Step 정의 ==========

	@Bean
	public Job dataProcessingJob(Step dataInitializationStep, Step dataProcessingStep, Step failStep,
								 Step contextPromotionStep, Step continuableStep, Step callableTaskletStep) {
		return new JobBuilder("dataProcessingJob", jobRepository)
				// 1. Validator: run.id 필수
				.validator(dataProcessingJobValidator())
				// 2. Incrementer: 동일 잡 재실행 시 run.id 자동 증가
				.incrementer(runIdIncrementer())
				// 3. JobExecutionListener
				.listener(batchPerformanceListener)
				.start(contextPromotionStep)  // 4. ExecutionContext 승격 학습
					.on("*")
					.to(continuableStep)      // 6. CONTINUABLE 학습
					.on("*")
					.to(callableTaskletStep)  // 7. CallableTaskletAdapter 학습
					.on("*")
					.to(dataInitializationStep)
					.on("FAILED")
					.to(failStep)
				.from(dataInitializationStep)
					.on("*")
					.to(dataProcessingStep)
						.on("FAILED")
						.to(failStep)
					.from(dataProcessingStep)
						.on("*")
						.end()
				.end()
				.build();
	}

	@Bean
	public Step contextPromotionStep() {
		return new StepBuilder("contextPromotionStep", jobRepository)
				.tasklet(contextPromotionTasklet(), transactionManager)
				.listener(executionContextPromotionListener())
				.build();
	}

	@Bean
	public Step continuableStep() {
		return new StepBuilder("continuableStep", jobRepository)
				.tasklet(continuableTasklet(), transactionManager)
				.build();
	}

	@Bean
	public Step callableTaskletStep() {
		return new StepBuilder("callableTaskletStep", jobRepository)
				.tasklet(callableTaskletAdapter(), transactionManager)
				.build();
	}

	@Bean
	public Step dataInitializationStep() {
		return new StepBuilder("dataInitializationStep", jobRepository)
				.tasklet(dataInitializationTasklet, transactionManager)
				.listener(batchPerformanceListener)
				.build();
	}

	@Bean
	public Step dataProcessingStep(@Qualifier("batchInputReader") JpaPagingItemReader<BatchInput> batchInputReader,
								   @Qualifier("batchInputJdbcCursorItemReader") JdbcCursorItemReader<BatchInput> batchInputJdbcCursorItemReader) {
		return new StepBuilder("dataProcessingStep", jobRepository)
				.<BatchInput, BatchOutput>chunk(CHUNK_SIZE)  // 정적 청크 크기
				.reader(batchInputJdbcCursorItemReader)
				.processor(apiCallItemProcessor)
				.writer(batchOutputWriter)
				.listener(batchPerformanceListener)
				.build();
	}

	/**
	 * 학습: CompletionPolicy - SimpleCompletionPolicy(5)와 동일하게 5개마다 커밋.
	 * chunk(size)는 내부적으로 SimpleCompletionPolicy 사용.
	 * TimeoutTerminationPolicy(3000): 3초 경과 시 청크 종료.
	 * CompositeCompletionPolicy: 여러 정책 조합 시 chunkOperations(RepeatOperations)로 설정.
	 */
	@Bean
	public Step completionPolicyStep(@Qualifier("batchInputJdbcCursorItemReader") JdbcCursorItemReader<BatchInput> batchInputJdbcCursorItemReader) {
		return new StepBuilder("completionPolicyStep", jobRepository)
				.<BatchInput, BatchOutput>chunk(5)  // SimpleCompletionPolicy(5)와 동일
				.reader(batchInputJdbcCursorItemReader)
				.processor(apiCallItemProcessor)
				.writer(batchOutputWriter)
				.build();
	}

	@Bean
	public Step failStep() {
		return new StepBuilder("failStep", jobRepository)
				.tasklet(failStepTasklet, transactionManager)
				.listener(batchPerformanceListener)
				.build();
	}
}
