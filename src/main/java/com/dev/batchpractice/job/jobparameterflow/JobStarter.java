package com.dev.batchpractice.job.jobparameterflow;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * ⚠️ incrementer + 외부 파라미터 + validator 동시 사용 이슈
 *
 * <p><b>이슈:</b><br>
 * - Job에 JobParametersIncrementer를 설정하고
 * - 외부에서 파라미터를 전달하여 실행하면
 * - 전달한 파라미터가 무시되는 현상이 발생함
 *
 * <p><b>원인 (Spring Batch 동작 규칙):</b><br>
 * - Job 실행 시 incrementer가 존재하면
 * - 전달된 JobParameters 대신 incrementer가 생성한 파라미터가 사용됨
 *
 * <pre>
 * start(job, externalParams)
 * → externalParams 무시
 * → incrementer.getNext(...) 결과만 사용
 * </pre>
 *
 * <p>즉,
 * <br>✔ 외부 파라미터는 Job 실행에 반영되지 않음
 * <br>✔ validator는 incrementer 결과만 검증하게 됨
 *
 * <p><b>결과:</b><br>
 * - 외부에서 전달한 mode, name 등의 값이 사라짐
 * - validator가 의도와 다르게 동작함
 * - 파라미터 기반 분기 로직이 깨짐
 *
 * <p><b>실무 적용 결정:</b><br>
 * 외부 파라미터를 사용하는 Job에서는 incrementer를 사용하지 않는다.
 *
 * <p>대신 실행 시점에 run.id 또는 timestamp 값을 직접 추가하여
 * 매 실행마다 새로운 JobInstance가 생성되도록 처리한다.
 *
 * <pre>
 * JobParameters params = new JobParametersBuilder()
 *     .addString("mode", ...)
 *     .addString("name", ...)
 *     .addLong("run.id", System.currentTimeMillis())
 *     .toJobParameters();
 * </pre>
 *
 * <p><b>정리:</b><br>
 * ✔ 외부 파라미터 기반 Job → incrementer 사용하지 않음<br>
 * ✔ 내부 자동 재실행 Job → incrementer 사용<br>
 */
// jobIncrementer + validator 동시 사용 이슈 학습용으로 만듦. .incrementer 활성화 + 이걸로 실행하면 외부 파라미터가 무시되는 현상을 볼 수 있음
//@Component
@RequiredArgsConstructor
public class JobStarter implements ApplicationRunner {

    private final JobOperator jobOperator;
    private final Job jobParameterFlowTaskletJob;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("mode", args.getOptionValues("mode").get(0))
                .addString("name", args.getOptionValues("name").get(0))
                .addDate("currentDate", new Date())
                .toJobParameters();

        jobOperator.start(jobParameterFlowTaskletJob, params);
    }
}
