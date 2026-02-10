package com.dev.batchpractice.job.dataprocessing.writer;

import com.dev.batchpractice.domain.entity.BatchOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchOutputWriter implements ItemWriter<BatchOutput> {

	@Override
	public void write(Chunk<? extends BatchOutput> chunk) {
		log.info("Writing {} BatchOutput items", chunk.getItems().size());
	}
}
