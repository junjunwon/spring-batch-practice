package com.dev.batchpractice.job.jobparameterflow.validator;

import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersValidator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
public class DeltaModeValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters parameters) throws InvalidJobParametersException {
        String mode = parameters.getString("mode");

        if ("DELTA".equals(mode)) {
            String startDate = parameters.getString("startDate");
            String endDate = parameters.getString("endDate");

            if (startDate == null || endDate == null) {
                throw new InvalidJobParametersException("DELTA mode requires startDate and endDate");
            }
        }
    }
}
