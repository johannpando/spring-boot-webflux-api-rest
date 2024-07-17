package com.johannpando.springboot.webflux.app.dto;

import com.johannpando.springboot.webflux.app.document.Product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ImageProductDTO {

	@Valid
	@NotNull
	private Product product;
	
	private String imageProduct;
}
