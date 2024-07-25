package com.johannpando.springboot.webflux.app.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.johannpando.springboot.webflux.app.document.Category;

public interface ICategoryDAO extends ReactiveMongoRepository<Category, String>{

}
