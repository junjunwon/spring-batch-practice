package com.dev.batchpractice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.ResultSet;

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

	@Column(name = "inputName", nullable = false)
	private String inputName;

	@Column(name = "data", nullable = false)
	private String data;

	@Column(name = "inputStatus", nullable = false)
	private Integer inputStatus;

	@Column(name = "processed", nullable = false)
	@Builder.Default
	private Boolean processed = false;

	public BatchInput(ResultSet rs) {
		try {
			this.id = rs.getLong("id");
//			this.inputName = rs.getString("input_name");
			this.data = rs.getString("data");
//			this.inputStatus = rs.getInt("input_status");
			this.processed = rs.getBoolean("processed");
		} catch (Exception e) {
			throw new RuntimeException("Error mapping ResultSet to BatchInput", e);
		}
	}
}
