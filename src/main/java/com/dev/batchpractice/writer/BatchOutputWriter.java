package com.dev.batchpractice.writer;

import com.dev.batchpractice.entity.BatchOutput;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchOutputWriter implements ItemWriter<BatchOutput> {

	private final EntityManagerFactory entityManagerFactory;

	@Override
	public void write(Chunk<? extends BatchOutput> chunk) throws Exception {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			for (BatchOutput item : chunk.getItems()) {
				if (item.getId() == null) {
					entityManager.persist(item);
				} else {
					entityManager.merge(item);
				}
			}
			entityManager.flush();
		} finally {
			if (entityManager.isOpen()) {
				entityManager.close();
			}
		}
	}
}
