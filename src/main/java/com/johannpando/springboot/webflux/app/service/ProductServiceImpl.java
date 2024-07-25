package com.johannpando.springboot.webflux.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.johannpando.springboot.webflux.app.dao.IProductDAO;
import com.johannpando.springboot.webflux.app.document.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements IProductService {

	@Autowired
	private IProductDAO productDAO;
	
	@Override
	public Flux<Product> findAll() {
		return productDAO.findAll();
	}

	@Override
	public Mono<Product> findById(String id) {
		return productDAO.findById(id);
	}

	@Override
	public Mono<Product> save(Product product) {
		return productDAO.save(product);
	}

	@Override
	public Mono<Void> delete(Product product) {
		return productDAO.delete(product);
	}

}
