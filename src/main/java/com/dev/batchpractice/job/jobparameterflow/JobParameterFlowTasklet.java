package com.dev.batchpractice.job.jobparameterflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@StepScope
public class JobParameterFlowTasklet implements Tasklet {

    private final String name;

    // 늦은 바인딩(Late Binding)
    public JobParameterFlowTasklet(@Value("#{jobParameters['name']}") String name) {
        this.name = name;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("=========================================");
        log.info("Spring Batch Job 실행 중...");
        log.info("Job Parameter name: {}", name);
        log.info("Step: {}", chunkContext.getStepContext().getStepName());
        log.info("Job: {}", chunkContext.getStepContext().getJobName());
        log.info("Job 실행 시간: {}", chunkContext.getStepContext().getStepExecution().getStartTime());
        log.info("=========================================");

        // JobExecution의 ExecutionContext 가져오기
        ExecutionContext jobContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        jobContext.put("user.name", name);

        // JobExecutionContext 정보 출력 (읽기 전용)
        Map<String, Object> jobExecutionContext = chunkContext.getStepContext().getJobExecutionContext();
        log.info("Job Execution Context: {}", jobExecutionContext);

        // jobExecutionContext 값 변경 -> 실패(정상)
//        jobExecutionContext.put("user.name", name + "_updated");
//        log.info("Updated Job Execution Context: {}", jobExecutionContext);

        // jobContext 값 변경 -> 성공(정상)
        jobContext.put("user.name", name + "_updated");
        log.info("Updated ExecutionContext in JobExecution: {}", jobContext);

        /**
         * on이 캐치하는 상태값이 BatchStatus가 아닌 ExitStatus이기 때문에
         * Step이 실패한 것으로 처리하려면 ExitStatus를 FAILED로 설정해야 한다.
         */
        contribution.setExitStatus(ExitStatus.FAILED);
        return RepeatStatus.FINISHED;
    }
}
