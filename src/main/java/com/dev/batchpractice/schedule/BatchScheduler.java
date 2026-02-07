package com.dev.batchpractice.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class BatchScheduler {

    private final JobOperator jobOperator; // 6 버전부터 JobLauncher -> JobOperator로 통합
    private final Job dataProcessingJob; // 배치잡 이름으로 식별됨

    @Scheduled(cron = "10 30 17 * * ?")
    public void runBatch() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        jobOperator.start(dataProcessingJob, jobParameters);
    }
}