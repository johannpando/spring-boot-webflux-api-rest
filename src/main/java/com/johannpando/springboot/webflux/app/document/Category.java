package com.johannpando.springboot.webflux.app.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "categories")
@Data
@NoArgsConstructor
public class Category {

	@Id
	private String id;
	
	@NotNull
	private String name;
	
	public Category(String name) {
		this.name = name;
	}
}
