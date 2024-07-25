package com.johannpando.springboot.webflux.app.service;

import com.johannpando.springboot.webflux.app.document.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductService {
	
	Flux<Product> findAll();
	
	Mono<Product> findById(String id);
	
	Mono<Product> save(Product product);
	
	Mono<Void> delete(Product product);
}
