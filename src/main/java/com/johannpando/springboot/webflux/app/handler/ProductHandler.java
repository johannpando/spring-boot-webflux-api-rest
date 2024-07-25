package com.johannpando.springboot.webflux.app.handler;

import java.net.URI;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.johannpando.springboot.webflux.app.document.Product;
import com.johannpando.springboot.webflux.app.dto.ImageProductDTO;
import com.johannpando.springboot.webflux.app.service.IProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

	@Autowired
	private IProductService productService;
	
	@Autowired
	private Validator validator;
	
	public Mono<ServerResponse> listAllProducts(ServerRequest request) {
		return ServerResponse
				.ok() // Indicate a successful response
				.contentType(MediaType.APPLICATION_JSON) // Set the response content type to JSON
				.body(productService.findAll(), Product.class); // Set the response body with the list of products
	}
	
	public Mono<ServerResponse> getProductById(ServerRequest request) {
		// Extract the 'id' path variable from the request
		String productId = request.pathVariable("id");
		
		return productService.findById(productId) // Fin the product by ID
			.flatMap(p -> 
				ServerResponse
					.ok() // Indicate a successful response
					.contentType(MediaType.APPLICATION_JSON) // Set the response content type to JSON
					.bodyValue(p) // Set the response with the found product					
			).switchIfEmpty(ServerResponse.notFound().build()); // If the product is not found, return a 404 response
	}
	
	public Mono<ServerResponse> createProduct(ServerRequest request) {
		
		
		// Get the product from request
		Mono<ImageProductDTO> dtoMono = request.bodyToMono(ImageProductDTO.class);
		
		return dtoMono.flatMap(dto -> {
			Product p = dto.getProduct();
			
			Errors errors = new BeanPropertyBindingResult(dto, ImageProductDTO.class.getName());
			validator.validate(dto, errors);
			
			if (errors.hasErrors()) {
				return Flux.fromIterable(errors.getFieldErrors())
					.map(fieldError -> "The field error " + fieldError.getField() + " " + fieldError.getDefaultMessage())
					.collectList()
					.flatMap(list -> ServerResponse.badRequest().bodyValue(list));
			} else {
				if (dto.getImageProduct() != null) {
					byte[] imageDecode = Base64.getDecoder().decode(dto.getImageProduct()); // Decode the base64 image
					p.setImage(imageDecode); // Set the decoded image to the product
				}
				
				if (p.getCreateAt() == null) {
					p.setCreateAt(new Date());
				}
				return productService.save(p)
					// We need to response with Mono<ServerResponse>
					.flatMap(pdb -> 
						ServerResponse
							// Indicate a resource creation response with the product ID in the URI
							.created(URI.create("/api/v2/product/".concat(pdb.getId())))
							.contentType(MediaType.APPLICATION_JSON) // Set the response content type to JSON
							.bodyValue(pdb) // Set the response body with the saved product
					);
			}
		});
	}
	
	public Mono<ServerResponse> updatedProduct(ServerRequest request) {
		// Get the product from request
		Mono<Product> productMono = request.bodyToMono(Product.class);
		// Extract the 'id' path variable from the request
		String productId = request.pathVariable("id");
		
		return productMono
			.flatMap(pm -> {
				Errors errors = new BeanPropertyBindingResult(pm, Product.class.getName());
				validator.validate(pm, errors);
				
				if (errors.hasErrors()) {
					return Flux.fromIterable(errors.getFieldErrors())
						.map(fieldError -> "The field error " + fieldError.getField() + " " + fieldError.getDefaultMessage())
						.collectList()
						.flatMap(list -> ServerResponse.badRequest().bodyValue(list));
				} else {
					// If the product it is not found, it returns a Mono.empty()
					Mono<Product> productFromBBDD = productService.findById(productId);
							
					// We combined the product from the BBDD and the product from request
					return productFromBBDD.zipWith(productMono, (db, req) ->{
						db.setName(req.getName());
						db.setCategory(req.getCategory());
						db.setPrice(req.getPrice());
						return db;
					})
					// Now, we proceed to save the product
					.flatMap(p ->
						ServerResponse
						// We redirect to the product detail through product id
						.created(URI.create("/api/v2/product/".concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON) // Set the response content type to JSON
						// We save the product
						.body(productService.save(p), Product.class)
					)
					// If the product it is not found, "productFromBBDD" return a Mono.empty, so execute this line
					.switchIfEmpty(ServerResponse.notFound().build());
				}
			});
	}
	
	public Mono<ServerResponse> deleteProduct(ServerRequest request) {
		
		// Get the product from request
		String productId = request.pathVariable("id");
		
		// If the product it is not found, it returns a Mono.empty()
		Mono<Product> productFromBBDD = productService.findById(productId);
		
		return productFromBBDD
			.flatMap(p -> 
				productService.delete(p).
				// The "delete" method return a Mono<Void>, 
				// so we invoke to the "then" method in order to create the Response
				then(ServerResponse
					.noContent()
					.build())
			)
			// If the product it is not found, "productFromBBDD" return a Mono.empty, so execute this line
			.switchIfEmpty(ServerResponse.notFound().build());
		
	}
}
