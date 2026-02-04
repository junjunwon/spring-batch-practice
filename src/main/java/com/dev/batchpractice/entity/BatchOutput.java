package com.dev.batchpractice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "batch_output")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchOutput {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "input_id", nullable = false)
	private Long inputId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String originalData;

	@Column(nullable = false)
	private String processedData;

	@Column(name = "api_response", columnDefinition = "TEXT")
	private String apiResponse;

	@Column(name = "processed_at", nullable = false)
	@Builder.Default
	private LocalDateTime processedAt = LocalDateTime.now();
}
