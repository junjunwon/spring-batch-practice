package com.dev.batchpractice.job.jobparameterflow.validator;

import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersValidator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(3)
@Component
public class FileModeValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters parameters) throws InvalidJobParametersException {
        String mode = parameters.getString("mode");

        if ("FILE".equals(mode)) {
            String fileName = parameters.getString("fileName");

            if (fileName == null) {
                throw new InvalidJobParametersException("FILE mode requires fileName");
            }
        }
    }
}
