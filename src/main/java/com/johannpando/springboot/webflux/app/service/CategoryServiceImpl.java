package com.johannpando.springboot.webflux.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.johannpando.springboot.webflux.app.dao.CategoryDAO;
import com.johannpando.springboot.webflux.app.document.Category;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryDAO categoryDAO;
	
	@Override
	public Flux<Category> findAll() {
		return categoryDAO.findAll();
	}

	@Override
	public Mono<Category> findById(String id) {
		return categoryDAO.findById(id);
	}

	@Override
	public Mono<Category> save(Category category) {
		return categoryDAO.save(category);
	}

}
