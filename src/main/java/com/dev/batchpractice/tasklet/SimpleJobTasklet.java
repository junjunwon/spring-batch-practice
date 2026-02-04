package com.dev.batchpractice.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimpleJobTasklet implements Tasklet {

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		log.info("=========================================");
		log.info("Spring Batch Job 실행 중...");
		log.info("Step: {}", chunkContext.getStepContext().getStepName());
		log.info("Job: {}", chunkContext.getStepContext().getJobName());
		log.info("Job 실행 시간: {}", chunkContext.getStepContext().getStepExecution().getStartTime());
		log.info("=========================================");
		
		// 간단한 처리 로직 예시
		log.info("작업 처리 중...");
		Thread.sleep(1000); // 1초 대기 (실제 작업 시뮬레이션)
		log.info("작업 완료!");

		/**
		 * on이 캐치하는 상태값이 BatchStatus가 아닌 ExitStatus이기 때문에
		 * Step이 실패한 것으로 처리하려면 ExitStatus를 FAILED로 설정해야 한다.
		 */
		contribution.setExitStatus(ExitStatus.FAILED);
		return RepeatStatus.FINISHED;
	}
}
