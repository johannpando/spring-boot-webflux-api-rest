package com.johannpando.springboot.webflux.app.rest.controller;

import java.net.URI;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.johannpando.springboot.webflux.app.document.Product;
import com.johannpando.springboot.webflux.app.dto.ImageProductDTO;
import com.johannpando.springboot.webflux.app.service.ProductService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {
	
	private static final Logger log = LoggerFactory.getLogger(ProductRestController.class);

	@Autowired
	private ProductService productService;
	
	@GetMapping()
	public Mono<ResponseEntity<Flux<Product>>> products() {
		return Mono.just(
			ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(productService.findAll())
		);
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Product>> product(@PathVariable String id) {
		return productService.findById(id)
			.map(p -> ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(p))
			.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@PostMapping
	public Mono<ResponseEntity<Product>> product(@Valid @RequestBody ImageProductDTO imageProductDTO) {
		
		Product product = imageProductDTO.getProduct();
		
		if (product.getCreateAt() == null) {
			product.setCreateAt(new Date());
		}
		
		if (imageProductDTO != null && imageProductDTO.getImageProduct() != null) {
			byte[] imageDecode = Base64.getDecoder().decode(imageProductDTO.getImageProduct());
			product.setImage(imageDecode);
		}
		
		return productService.save(product)
				.doOnSuccess(productSaved -> log.info("The product " + productSaved.getName() + " has been created"))
			.map(p -> ResponseEntity.created(URI.create("api/products/".concat(p.getId())))
					.contentType(MediaType.APPLICATION_JSON)
				.body(p)
			);
	}
	
	@PostMapping("/valid")
	public Mono<ResponseEntity<Map<String, Object>>> productValid(@Valid @RequestBody Mono<ImageProductDTO> imageProductDTOMono) {
		
		Map<String, Object> result = new HashMap<>();
		
		return imageProductDTOMono
			.flatMap(dto -> {
				Product product = dto.getProduct();
				if (product.getCreateAt() == null) {
					product.setCreateAt(new Date());
				}
				
				if (dto.getImageProduct() != null) {
					byte[] imageDecode = Base64.getDecoder().decode(dto.getImageProduct());
					product.setImage(imageDecode);
				}
				
				return productService.save(product)
					.map(p -> {
						String successMessage = "The product" + p.getName() + " was created successfully";
						log.info(successMessage);
						result.put("product", p);
						result.put("message", successMessage);
						result.put("timestamp", new Date());
						return ResponseEntity.created(URI.create("api/products/".concat(p.getId())))
								.contentType(MediaType.APPLICATION_JSON)
							.body(result);
					});
					
			})
			// onErrorResume(t -> { ... }): Handles errors that may occur during processing.
			.onErrorResume(t -> {
				// Mono.just(t).cast(WebExchangeBindException.class): Converts the error to WebExchangeBindException.
				return Mono.just(t).cast(WebExchangeBindException.class)
					// flatMap(e -> Mono.just(e.getFieldErrors())): Gets field errors
					.flatMap(e -> Mono.just(e.getFieldErrors()))
					// flatMapMany(Flux::fromIterable): Converts the list of field errors to a Flux.
					.flatMapMany(Flux::fromIterable)
					// Maps each field error to a readable error message.
					.map(fieldError -> "The field " + fieldError.getField() + " " + fieldError.getDefaultMessage())
					// It collects all error messages in a list and adds them to the results map.
					.collectList()
					.flatMap(list -> {
						result.put("errors", list);
						result.put("timestamp", new Date());
						result.put("status", HttpStatus.BAD_REQUEST.value());
						return Mono.just(ResponseEntity.badRequest().body(result));
					});
			});
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Product>> product(@RequestBody Product product, @PathVariable String id) {
		return productService.findById(id)
			.flatMap(p -> {
				if (product.getName() != null) {
					product.setName(product.getName());
				}
				if (product.getPrice() != null) {
					p.setPrice(product.getPrice());
				}
				if (product.getCategory() != null) {
					p.setCategory(product.getCategory());	
				}
				return productService.save(p)
					.doOnSuccess(productSaved -> log.info("The product " + productSaved.getName() + " has been updated"));
			})
			.map(p -> ResponseEntity.created(URI.create("api/products/".concat(p.getId())))
					.contentType(MediaType.APPLICATION_JSON)
				.body(p))
			.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	
	@DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
        return productService.findById(id)
            .flatMap(p -> {
                log.info("Deleting product with ID {} and name {}", p.getId(), p.getName());
                return productService.delete(p)
                	// 	After the operation completes (then), a ‘Mono’ is returned which emits a ‘ResponseEntity’ 
                	// with HTTP status No Content (204), indicating that the deletion was successful 
                	// and there is no additional content in the response.
                    .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
            })
            // If the initial ‘Mono<Product>’ is empty (i.e. if the product with the specified ID is not found), 
            // the block ‘switchIfEmpty’ is executed.
            .switchIfEmpty(
            	// Within switchIfEmpty, Mono.fromRunnable is used to create a Mono that executes a task 
            	// (in this case, logging a warning that the product with the specified ID was not found). 
            	// Mono.fromRunnable does not emit any value, it just executes the task.	
                Mono.fromRunnable(() -> log.warn("Product with ID {} not found", id))
                    .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NOT_FOUND)))
            );
    }
}
