package com.dev.batchpractice.reader;

import com.dev.batchpractice.entity.BatchInput;
import com.dev.batchpractice.reader.mapper.BatchInputRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
public class BatchInputJdbcCursorReader {

    private final DataSource dataSource;
    private final ArgumentPreparedStatementSetter processedFalseSetter;

    @Bean
    @StepScope
    public JdbcCursorItemReader<BatchInput> batchInputJdbcCursorItemReader() {
        return new JdbcCursorItemReaderBuilder<BatchInput>()
                .name("batchInputJdbcCursorItemReader")
                .dataSource(dataSource)
                .sql("SELECT id, data, processed FROM batch_input WHERE processed = ? ORDER BY id")
                .rowMapper(new BatchInputRowMapper())
                .preparedStatementSetter(processedFalseSetter)
                .build();
    }

    /**
     * RowMapper가 resultSet 로우를 도메인 객체로 매핑하는 반면,
     * PreparedStatementSetter는 파라미터를 SQL문에 매핑하는 역할을 한다.
     * ArgumentPreparedStatementSetter 인스턴스는 객체 배열을 파라미터를 전달받음.
     *
     * 1) 배열에 담긴 객체가 SqlParameterValue 인스턴스 타입 ->
     *   SqlParameterValue 타입에는 값을 설정하는 방법 (값을 설정할 인덱스, 값의 타입 등)이 담긴 메타데이터가 포함돼 있다.
     *   이 경우, 스프링은 해당 메타데이터에 정의된 내용에 따라 파라미터를 설정한다.
     * 2) 배열에 담긴 객체가 SqlParameterValue 인스턴스 타입이 아닌 경우 ->
     *   해당 객체는 담긴 순서대로 PreparedStatement의 ?의 위치에 값으로 설정된다.
     */
    @Bean
    @StepScope
    public ArgumentPreparedStatementSetter processedFalseSetter() {
        return new ArgumentPreparedStatementSetter(new Object[] { false });
    }
}
