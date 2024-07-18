package com.johannpando.springboot.webflux.app.handler;

import java.net.URI;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.johannpando.springboot.webflux.app.document.Product;
import com.johannpando.springboot.webflux.app.dto.ImageProductDTO;
import com.johannpando.springboot.webflux.app.service.ProductService;

import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

	@Autowired
	private ProductService productService;
	
	public Mono<ServerResponse> listAllProducts(ServerRequest request) {
		return ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(productService.findAll(), Product.class);
	}
	
	public Mono<ServerResponse> getProductById(ServerRequest request) {
		
		String productId = request.pathVariable("id");
		
		return productService.findById(productId)
			.flatMap(p -> 
				ServerResponse
					.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(p)					
			).switchIfEmpty(ServerResponse.notFound().build());
	}
	
	public Mono<ServerResponse> createProduct(ServerRequest request) {
		// Get the product from request
		Mono<ImageProductDTO> dtoMono = request.bodyToMono(ImageProductDTO.class);
		
		return dtoMono.flatMap(dto -> {
			Product p = dto.getProduct();
			if (dto.getImageProduct() != null) {
				byte[] imageDecode = Base64.getDecoder().decode(dto.getImageProduct());
				p.setImage(imageDecode);
			}
			
			if (p.getCreateAt() == null) {
				p.setCreateAt(new Date());
			}
				return productService.save(p);
			})
			// We need to response with Mono<ServerResponse>
			.flatMap(p -> 
				ServerResponse
					.created(URI.create("/api/v2/product/".concat(p.getId())))
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(p)
			);
	}
	
	public Mono<ServerResponse> updatedProduct(ServerRequest request) {
		// Get the product from request
		Mono<Product> productMono = request.bodyToMono(Product.class);
		String productId = request.pathVariable("id");
		
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
			.contentType(MediaType.APPLICATION_JSON)
			// We save the product
			.body(productService.save(p), Product.class)
		)
		// If the product it is not found, "productFromBBDD" return a Mono.empty, so execute this line
		.switchIfEmpty(ServerResponse.notFound().build());
		
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