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
public class FailStepTasklet implements Tasklet {

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		log.error("=========================================");
		log.error("Job 실행 중 오류 발생!");
		log.error("Step: {}", chunkContext.getStepContext().getStepName());
		log.error("Job: {}", chunkContext.getStepContext().getJobName());
		log.error("이전 Step에서 실패가 발생했습니다.");
		log.error("=========================================");
		
		// ExitStatus를 FAILED로 설정하여 Job 실패 처리
		contribution.setExitStatus(ExitStatus.FAILED);
		
		// 예외를 던져서 Job을 실패 상태로 만듦
		throw new RuntimeException("Job execution failed. Previous step encountered an error.");
	}
}
