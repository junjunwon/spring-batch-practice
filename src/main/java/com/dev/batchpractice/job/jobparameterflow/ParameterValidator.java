package com.dev.batchpractice.job.jobparameterflow;

import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersValidator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ParameterValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters parameters) throws InvalidJobParametersException {
        String name = parameters.getString("name");

        if (!StringUtils.hasText(name)) {
            throw new InvalidJobParametersException("The 'name' parameter is required and cannot be empty.");
        }
    }
}
