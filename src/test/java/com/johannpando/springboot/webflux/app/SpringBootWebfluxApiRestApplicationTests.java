package com.johannpando.springboot.webflux.app;

import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.johannpando.springboot.webflux.app.document.Category;
import com.johannpando.springboot.webflux.app.document.Product;
import com.johannpando.springboot.webflux.app.dto.ImageProductDTO;
import com.johannpando.springboot.webflux.app.service.ICategoryService;
import com.johannpando.springboot.webflux.app.service.IProductService;

import reactor.core.publisher.Mono;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Initiate a real server with a random port 
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Mock a environment, faster than the previous one
class SpringBootWebfluxApiRestApplicationTests {
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApiRestApplicationTests.class);

	// Reactive class for tests purpose
	@Autowired
	private WebTestClient webTestClient;
	
	@Autowired
	private IProductService productService;
	
	@Autowired
	private ICategoryService categoryService;
	
	@Test
	public void getAllProducts() {
		
		webTestClient
		.get() // Make a GET request 
		.uri("/api/v2/products") // Specifies a URI to run the test
		.accept(MediaType.APPLICATION_JSON) // Specifies that a JSON is accepted as a response
		.exchange() // Send and receive the answer
		.expectStatus().isOk() // Verify that the response is a 200 OK
		.expectHeader().contentType(MediaType.APPLICATION_JSON) // Verify that the header of the response is a JSON
		.expectBodyList(Product.class) // Expect the body of the response to be a list of products.
		.consumeWith(response -> { // Consume the response to make the relevant assertions
			List<Product> products = response.getResponseBody(); // Extract the body of the response as a list of products
			Assertions.assertThat(products.isEmpty()).isFalse(); // Verify the list of products is not empty
		});
	}
	
	@Test
	public void getProductById() {
		
		webTestClient
		.get() // Make a GET request 
		.uri("/api/v2/products") // Specifies a URI to run the test
		.accept(MediaType.APPLICATION_JSON) // Specifies that a JSON is accepted as a response
		.exchange() // Send and receive the answer
		.expectStatus().isOk() // Verify that the response is a 200 OK
		.expectHeader().contentType(MediaType.APPLICATION_JSON) // Verify that the header of the response is a JSON
		.expectBodyList(Product.class) // Expect the body of the response to be a list of products.
		.consumeWith(response -> { // Consume the response to make the relevant assertions
			List<Product> products = response.getResponseBody(); // Extract the body of the response as a list of products
			Assertions.assertThat(products.isEmpty()).isFalse(); // Verify the list of products is not empty
			Product p = products.get(0); // Retrieve the first element
			webTestClient.get() // Make a GET request
			.uri("/api/v2/products/{id}", Collections.singletonMap("id", p.getId())) // Specifies the URI with the product ID
			.accept(MediaType.APPLICATION_JSON) // Specifies that a JSON is accepted as a response
			.exchange() // Send and receive the answer
			.expectStatus().isOk() // Verify that the response is a 200 OK
			.expectHeader().contentType(MediaType.APPLICATION_JSON) // Verify that the header of the response is a JSON
			.expectBody(Product.class) // Expect the body of the response to be a Product class
			.consumeWith(r -> { // Consume the response
				Product p2 = r.getResponseBody();
				log.info("The product with id {} is found", p2.getId());
				Assertions.assertThat(p2.getId().isEmpty()).isFalse(); // Verify the product ID is not empty
			});
		});
	}
	
	@Test
	public void getProductByIdJsonPath() {
		
		webTestClient
		.get() // Make a GET request 
		.uri("/api/v2/products") // Specifies a URI to run the test
		.accept(MediaType.APPLICATION_JSON) // Specifies that a JSON is accepted as a response
		.exchange() // Send and receive the answer
		.expectStatus().isOk() // Verify that the response is a 200 OK
		.expectHeader().contentType(MediaType.APPLICATION_JSON) // Verify that the header of the response is a JSON
		.expectBodyList(Product.class) // Expect the body of the response to be a list of products.
		.consumeWith(response -> { // Consume the response to make the relevant assertions
			List<Product> products = response.getResponseBody(); // Extract the body of the response as a list of products
			Assertions.assertThat(products.isEmpty()).isFalse(); // Verify the list of products is not empty
			Product p = products.get(0); // Retrieve the first element
			webTestClient.get() // Make a GET request
			.uri("/api/v2/products/{id}", Collections.singletonMap("id", p.getId())) // Specifies the URI with the product ID
			.accept(MediaType.APPLICATION_JSON) // Specifies that a JSON is accepted as a response
			.exchange() // Send and receive the answer
			.expectStatus().isOk() // Verify that the response is a 200 OK
			.expectHeader().contentType(MediaType.APPLICATION_JSON) // Verify that the header of the response is a JSON
			.expectBody()
			.jsonPath("$.id").isNotEmpty();
		});
	}

	@Test
	public void updateProductTest() {
		
		// The method findAll return a Flux<Class> that can emit multiple elements
		// blockFirst convert the reactive Flux to a block method, getting a first element of the Class
		// blockFirst is used to tests class
		Product product = productService.findAll().blockFirst();
		Category category = categoryService.findAll().blockFirst();
		
		Product updatedProduct = new Product("New Product 365", 2500, category);
		
		webTestClient
		.put() // Make a PUT request 
		.uri("/api/v2/products/{id}", Collections.singletonMap("id", product.getId())) // Specifies a URI to run the test
		.accept(MediaType.APPLICATION_JSON) // Specifies that a JSON is accepted as a response
		.body(Mono.just(updatedProduct), Product.class) // Specifies the product as a body
		.exchange() // Send and receive the answer
		.expectStatus().isCreated() // Verify that the response is a 201 OK
		.expectHeader().contentType(MediaType.APPLICATION_JSON) // Verify that the header of the response is a JSON
		.expectBody() // Expect the body of the response
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.name").isEqualTo("New Product 365")
		.jsonPath("$.price").isEqualTo(2500);
	}
	
	@Test
	public void deleteProductTest() {
		// The method findAll return a Flux<Class> that can emit multiple elements
		// blockFirst convert the reactive Flux to a block method, getting a first element of the Class
		// blockFirst is used to tests class
		Product product = productService.findAll().blockFirst();
		
		String productId = product.getId();
		
		webTestClient
		.delete()
		.uri("/api/v2/products/{id}", Collections.singletonMap("id", productId)) // Specifies a URI to run the test
		.exchange() // Send and receive the answer
		.expectStatus().isNoContent() // If the product has been eliminated we expect the No Content status
		.expectBody() // and an empty body
		.isEmpty();
		
		webTestClient
		.get()
		.uri("/api/v2/productos/{id}", Collections.singletonMap("id", productId))
		.exchange()
		.expectStatus().isNotFound(); // If the product has been eliminated and we tried to find, the response should be Not Found status
		//.expectBody()
		//.isEmpty();
	}
	
	@Test
	public void addProductTest() {
		// The method findAll return a Flux<Class> that can emit multiple elements
		// blockFirst convert the reactive Flux to a block method, getting a first element of the Class
		// blockFirst is used to tests class
		
		Category category = categoryService.findAll().blockFirst();
		
		Product newProduct = new Product("New Product 1024", 1024, category);
		
		ImageProductDTO dtoProduct = new ImageProductDTO();
		dtoProduct.setProduct(newProduct);
		
		webTestClient
		.post()
		.uri("/api/v2/products") // Specifies a URI to run the test
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(dtoProduct), ImageProductDTO.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.name").isEqualTo("New Product 1024")
		.jsonPath("$.price").isEqualTo(1024);
	}
	
}
