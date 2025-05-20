package com.example.productservice;

import com.example.productservice.DTO.ProductDTO;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductFuzzySearch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT )
class ProductServiceApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16.0");

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ProductRepository productRepository;

	@BeforeEach
	void setUp() {
		// Add product to database before test
		productRepository.saveAll(List.of(new Product("12345", "Fan", "Sample product", "10", 10),
					new Product("1235", "Controller", "Sample product", "10", 10)));
	}

	@Test
	void testGetProduct() {
		ResponseEntity<Product> response = restTemplate.getForEntity("/api/v1/products/get/Fan", Product.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getName()).isEqualTo("Fan");
	}
	@Test
	void testGetProductById() {
		ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/products/getId/12345", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		Assertions.assertNotNull(response.getBody());
		assertThat(response.getBody()).isEqualTo("12345");
	}
	@Test
	void testGetProductByIdNotExist() {
		ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/products/getId/1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void testGetAllProducts() {
		ResponseEntity<ProductDTO[]> response = restTemplate.getForEntity("/api/v1/products/getAll", ProductDTO[].class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().length).isEqualTo(2);
		assertThat(response.getBody()[0].name()).isEqualTo("Fan");
	}

	@Test
	void testCreateProduct() {

	}

}
