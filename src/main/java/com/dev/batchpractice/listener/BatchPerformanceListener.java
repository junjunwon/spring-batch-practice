package com.dev.batchpractice.listener;

import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class BatchPerformanceListener implements JobExecutionListener, StepExecutionListener {

	private Instant jobStartTime;
	private Instant stepStartTime;

	@Override
	public void beforeJob(JobExecution jobExecution) {
		jobStartTime = Instant.now();
		log.info("Job {} started at {}", jobExecution.getJobInstance().getJobName(), jobStartTime);
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		Instant endTime = jobExecution.getEndTime() != null
				? jobExecution.getEndTime().atZone(ZoneId.systemDefault()).toInstant()
				: Instant.now();
		long durationMs = Duration.between(jobStartTime, endTime).toMillis();
		String status = jobExecution.getExitStatus().getExitCode();
		log.info("[Job Performance] jobName={}, status={}, duration={} ({}ms)",
				jobExecution.getJobInstance().getJobName(), status, formatDuration(durationMs), durationMs);
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		stepStartTime = Instant.now();
		log.info("Step {} started at {}", stepExecution.getStepName(), stepStartTime);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		Instant endTime = stepExecution.getEndTime() != null
				? stepExecution.getEndTime().atZone(ZoneId.systemDefault()).toInstant()
				: Instant.now();
		long durationMs = Duration.between(stepStartTime, endTime).toMillis();
		double durationSec = durationMs / 1000.0;
		long readCount = stepExecution.getReadCount();
		long writeCount = stepExecution.getWriteCount();
		long skipCount = stepExecution.getSkipCount();
		long commitCount = stepExecution.getCommitCount();
		double throughput = durationSec > 0 ? writeCount / durationSec : 0;

		log.info("[Step Performance] stepName={}, duration={} ({}ms), read={}, write={}, skip={}, commit={}, throughput={}/sec",
				stepExecution.getStepName(), formatDuration(durationMs), durationMs, readCount, writeCount, skipCount, commitCount,
				String.format("%.2f", throughput));

		return stepExecution.getExitStatus();
	}

	/**
	 * 소요 시간(ms)을 분·초·밀리초 단위로 읽기 쉽게 포맷한다.
	 * 예: 125000ms → "2m 5s 0ms", 3500ms → "3s 500ms", 99ms → "99ms"
	 */
	private String formatDuration(long durationMs) {
		long minutes = durationMs / 60_000;
		long seconds = (durationMs % 60_000) / 1_000;
		long ms = durationMs % 1_000;
		if (minutes > 0) {
			return String.format("%dm %ds %dms", minutes, seconds, ms);
		}
		if (seconds > 0) {
			return String.format("%ds %dms", seconds, ms);
		}
		return durationMs + "ms";
	}
}
