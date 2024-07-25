# Summary of Spring Boot with WebFlux

## Why Use WebFlux

### Reactive Programming
- **Non-blocking I/O**: WebFlux is built on Project Reactor, which provides a reactive programming model that handles non-blocking I/O operations, making it ideal for applications that need to handle a large number of concurrent connections with minimal resource consumption.
- **Backpressure Handling**: Reactive Streams API, which WebFlux supports, provides backpressure mechanisms to handle streams of data in a controlled manner, preventing overwhelming consumers.
- **Scalability**: By using reactive programming, WebFlux applications can scale more efficiently, utilizing fewer threads and making better use of system resources compared to traditional blocking I/O.

### Flexibility
- **Annotation-based and Functional Endpoints**: WebFlux supports both traditional annotation-based controllers and functional endpoints, giving developers flexibility in choosing their preferred style.
- **WebClient**: A non-blocking, reactive alternative to RestTemplate, WebClient allows for more efficient inter-service communication within microservices architectures.

## Key Commands and Concepts

### Setting Up WebFlux
- **Dependencies**: Ensure you have the necessary dependencies in your `pom.xml` or `build.gradle` file for Spring WebFlux and Reactor.

### Annotation-based Controllers
- **@RestController**: Used to define RESTful web services.
- **@GetMapping, @PostMapping, @PutMapping, @DeleteMapping**: Used to map HTTP GET, POST, PUT, and DELETE requests to specific handler methods.
- **@RequestBody**: Binds the body of the HTTP request to a method parameter.
- **@PathVariable**: Extracts values from the URI path.

### Functional Endpoints
- **RouterFunctions.route()**: Defines routing rules for handling requests in a functional style.
- **ServerRequest and ServerResponse**: Represent the HTTP request and response in functional endpoints.
- **RequestPredicates**: Provides various methods to match requests (e.g., GET, POST, path patterns).

### WebClient
- **WebClient.create()**: Initializes a WebClient instance.
- **retrieve()**: Initiates the request and retrieves the response.
- **bodyToMono(), bodyToFlux()**: Convert the response body to a Mono or Flux.
- **exchange()**: (Deprecated) Initiates the request and allows handling of the full response.

### Error Handling
- **onErrorResume()**: Provides an alternative Mono/Flux in case of an error.
- **switchIfEmpty()**: Provides an alternative Mono/Flux if the original completes empty.
- **ServerResponse.status()**: Sets the HTTP status code of the response.
- **bodyValue()**: Sets the body of the response.

### Testing with WebTestClient
- **WebTestClient**: A non-blocking, reactive client used for testing WebFlux applications.
- **exchange()**: Executes the request and returns a response spec for further assertions.
- **expectStatus()**: Asserts the expected status of the response.
- **expectHeader()**: Asserts the expected headers in the response.
- **expectBody()**: Asserts the expected body content.

### Example Code
#### Annotation-based Controller Method
```java
@PutMapping("/{id}")
public Mono<ResponseEntity<Product>> updateProduct(@RequestBody Product product, @PathVariable String id) {
    return productService.findById(id)
        .flatMap(existingProduct -> {
            existingProduct.setName(product.getName());
            existingProduct.setPrice(product.getPrice());
            return productService.save(existingProduct);
        })
        .map(updatedProduct -> ResponseEntity.ok(updatedProduct))
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
}
```

#### Functional Endpoint
```java
public Mono<ServerResponse> getProductById(ServerRequest request) {
    String id = request.pathVariable("id");
    return productService.findById(id)
        .flatMap(product -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(product))
        .switchIfEmpty(ServerResponse.notFound().build());
}
```

#### WebClient Usage
```java
public Mono<Product> getProductById(String id) {
    return webClient.get().uri("/{id}", id)
        .retrieve()
        .bodyToMono(Product.class);
}
```

#### WebTestClient Test
```java
@Test
public void testGetAllProducts() {
    webTestClient.get().uri("/api/v2/products")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(Product.class)
        .consumeWith(response -> {
            List<Product> products = response.getResponseBody();
            Assertions.assertThat(products).isNotEmpty();
        });
}
```

By using Spring WebFlux, you can build scalable, efficient, and flexible web applications that handle high concurrency with ease. The reactive programming paradigm enables you to write non-blocking, event-driven code that makes better use of system resources, leading to improved performance and scalability.