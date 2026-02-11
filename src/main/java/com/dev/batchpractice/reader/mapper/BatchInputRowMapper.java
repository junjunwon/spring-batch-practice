package com.dev.batchpractice.reader.mapper;

import com.dev.batchpractice.entity.BatchInput;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BatchInputRowMapper implements RowMapper<BatchInput> {
    @Override
    public BatchInput mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new BatchInput(rs);
    }
}
