package com.dev.batchpractice.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Entity
@Table(name = "batch_input")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BatchInput {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String data;

	@Column(nullable = false)
	private Integer status;

	@Column(name = "processed", nullable = false)
	@Builder.Default
	private Boolean processed = false;
}
