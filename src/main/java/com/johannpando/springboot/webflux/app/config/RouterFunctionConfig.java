package com.johannpando.springboot.webflux.app.config;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.johannpando.springboot.webflux.app.handler.ProductHandler;

@Configuration
public class RouterFunctionConfig {

    @Bean
    RouterFunction<ServerResponse> routes(ProductHandler handler) {
		return route(GET("/api/v2/products").or(GET("/api/v3/products")), 
				//request -> handler.listAllProducts(request));
				// It is the same
				handler::listAllProducts)
				.andRoute(GET("/api/v2/products/{id}"), handler::getProductById)
				.andRoute(POST("/api/v2/products"), handler::createProduct)
				.andRoute(PUT("/api/v2/products/{id}"), handler::updatedProduct)
				.andRoute(DELETE("/api/v2/products/{id}"), handler::deleteProduct);
	}
}
