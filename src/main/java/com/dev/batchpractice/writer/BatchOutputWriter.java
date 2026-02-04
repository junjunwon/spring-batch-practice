package com.dev.batchpractice.writer;

import com.dev.batchpractice.entity.BatchOutput;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchOutputWriter implements ItemWriter<BatchOutput> {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public void write(Chunk<? extends BatchOutput> chunk) throws Exception {
		for (BatchOutput item : chunk.getItems()) {
			if (item.getId() == null) {
				entityManager.persist(item);
			} else {
				entityManager.merge(item);
			}
		}
		entityManager.flush();
	}
}
