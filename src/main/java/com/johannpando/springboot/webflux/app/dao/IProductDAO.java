package com.johannpando.springboot.webflux.app.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.johannpando.springboot.webflux.app.document.Product;

public interface IProductDAO extends ReactiveMongoRepository<Product, String>{
	
	
}
