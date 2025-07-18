package com.example.productservice;

import com.example.productservice.DTO.ProductDTO;
import com.example.productservice.DTO.ProductRequestDTO;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes ={ProductServiceApplication.class,TestContainersConfig.class} )
class ProductServiceApplicationTests {


	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ProductRepository productRepository;

//	@MockitoBean
//	private StreamBridge streamBridge;

	private final ProductRequestDTO productRequestDTO= new ProductRequestDTO("Shoe", "Test", "10", 10);

	@BeforeEach
	void setUp() {
		// Add product to database before test
		productRepository.saveAll(List.of(new Product("12345", "Fan", "Sample product", "10", 10),
					new Product("1235", "2", "Sample product", "10", 10)));


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

//	@Test
//	void testCreateProduct() {
//		ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/products/create",productRequestDTO, String.class);
//		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//		assertThat(response.getBody()).isNotNull();
//		assertThat(response.getBody()).isEqualTo("Product created");
//	when(streamBridge.send())
//	}
//	@Test
//	void testDeleteProduct() {
//
//	}

}
