package com.johannpando.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.johannpando.springboot.webflux.app.document.Category;
import com.johannpando.springboot.webflux.app.document.Product;
import com.johannpando.springboot.webflux.app.service.CategoryService;
import com.johannpando.springboot.webflux.app.service.ProductService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApiRestApplication implements CommandLineRunner {
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApiRestApplication.class);
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ReactiveMongoTemplate reactiveMongoTemplate;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApiRestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		create();
	}

	private void create() {
		
		// We drop the collection before it is created
		reactiveMongoTemplate.dropCollection("products")
		// If we do not subscribe, nothing will happen
		.subscribe();
		
		reactiveMongoTemplate.dropCollection("categories")
		.subscribe();
		
		Category mobilePhone = new Category("Mobile Phone");
		Category computer = new Category("Computer");
		Category others = new Category("Others");
		
		Flux.just(mobilePhone, computer, others)
		.flatMap(category -> {
			return categoryService.save(category);
		})
		//.subscribe(category -> log.info("The category with name " + category.getName() + " has been created"));
		.doOnNext(category -> {
			log.info("The category with name " + category.getName() + " has been created");
		}).thenMany(
			Flux.just(new Product("IPhone 5", 450.89, mobilePhone),
					new Product("IPhone 6", 500.89, mobilePhone),
					new Product("Iphone 7", 790.90, mobilePhone))
			//.map(product -> productDAO.save(product)) //Return the Mono<T>
			.flatMap(product -> {
				product.setCreateAt(new Date());
				return productService.save(product);
			})
		)
		.subscribe(product -> log.info("Insert: " + product.getId() + " " + product.getName()));
	}
}
