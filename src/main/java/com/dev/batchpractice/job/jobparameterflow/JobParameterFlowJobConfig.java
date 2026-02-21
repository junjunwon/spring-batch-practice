package com.dev.batchpractice.job.jobparameterflow;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JobParameterFlowJobConfig {

    private final JobRepository jobRepository;
    private final JobParameterFlowTasklet jobParameterFlowTasklet;
    private final ParameterValidator parameterValidator;

    @Bean
    public Job simpleTaskletJob() {
        return new JobBuilder("simpleTaskletJob", jobRepository)
            .start(simpleTaskletStep())
            .validator(parameterValidator)
            .build();
    }

    @Bean
    public Step simpleTaskletStep() {
        return new StepBuilder("simpleTaskletStep", jobRepository)
            .tasklet(jobParameterFlowTasklet)
            .build();
    }
}
