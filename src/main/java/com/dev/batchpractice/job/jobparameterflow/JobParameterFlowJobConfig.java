package com.dev.batchpractice.job.jobparameterflow;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.CompositeJobParametersValidator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JobParameterFlowJobConfig {

    private final JobRepository jobRepository;
    private final JobParameterFlowTasklet jobParameterFlowTasklet;
    private final @Qualifier("parametersValidator")
    CompositeJobParametersValidator parameterValidator;

    @Bean
    public Job jobParameterFlowTaskletJob() {
        return new JobBuilder("jobParameterFlowTaskletJob", jobRepository)
                .start(jobParameterFlowStep())
                .validator(parameterValidator)
                .build();
    }

    @Bean
    public Step jobParameterFlowStep() {
        return new StepBuilder("jobParameterFlowStep", jobRepository)
                .tasklet(jobParameterFlowTasklet)
                .build();
    }
}
