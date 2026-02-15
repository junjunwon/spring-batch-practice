package com.dev.batchpractice.job.jobparameterflow.validator;

import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersValidator;

import java.util.Set;

public class ModeValidator implements JobParametersValidator {

    private static final Set<String> VALID_MODES = Set.of("FULL", "DELTA", "FILE");

    @Override
    public void validate(JobParameters parameters) throws InvalidJobParametersException {
        String mode = parameters.getString("mode");

        if (mode == null || !VALID_MODES.contains(mode.toUpperCase())) {
            throw new InvalidJobParametersException("Invalid mode parameter. Allowed values are: " + VALID_MODES);
        }

    }
}
