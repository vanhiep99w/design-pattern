package com.designpatterns.showcase.common.repository;

import com.designpatterns.showcase.common.domain.Product;
import com.designpatterns.showcase.common.domain.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testProduct = Product.builder()
                .name("Test Product")
                .description("A test product")
                .price(new BigDecimal("99.99"))
                .category(ProductCategory.ELECTRONICS)
                .stockQuantity(10)
                .available(true)
                .build();
    }

    @Test
    void shouldSaveProduct() {
        Product savedProduct = productRepository.save(testProduct);

        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Test Product");
        assertThat(savedProduct.getCreatedAt()).isNotNull();
        assertThat(savedProduct.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindByCategory() {
        productRepository.save(testProduct);

        Product clothingProduct = Product.builder()
                .name("T-Shirt")
                .description("A shirt")
                .price(new BigDecimal("29.99"))
                .category(ProductCategory.CLOTHING)
                .stockQuantity(50)
                .available(true)
                .build();
        productRepository.save(clothingProduct);

        List<Product> electronics = productRepository.findByCategory(ProductCategory.ELECTRONICS);

        assertThat(electronics).hasSize(1);
        assertThat(electronics.get(0).getName()).isEqualTo("Test Product");
    }

    @Test
    void shouldFindAvailableProducts() {
        testProduct.setAvailable(true);
        productRepository.save(testProduct);

        Product unavailableProduct = Product.builder()
                .name("Unavailable Product")
                .description("Out of stock")
                .price(new BigDecimal("49.99"))
                .category(ProductCategory.GENERAL)
                .stockQuantity(0)
                .available(false)
                .build();
        productRepository.save(unavailableProduct);

        List<Product> availableProducts = productRepository.findByAvailableTrue();

        assertThat(availableProducts).hasSize(1);
        assertThat(availableProducts.get(0).getName()).isEqualTo("Test Product");
    }

    @Test
    void shouldFindByNameContaining() {
        productRepository.save(testProduct);

        Product anotherProduct = Product.builder()
                .name("Another Test Item")
                .description("Another item")
                .price(new BigDecimal("19.99"))
                .category(ProductCategory.GENERAL)
                .stockQuantity(5)
                .available(true)
                .build();
        productRepository.save(anotherProduct);

        List<Product> products = productRepository.findByNameContainingIgnoreCase("test");

        assertThat(products).hasSize(2);
    }

    @Test
    void shouldFindByStockQuantityGreaterThan() {
        testProduct.setStockQuantity(10);
        productRepository.save(testProduct);

        Product lowStockProduct = Product.builder()
                .name("Low Stock Product")
                .description("Almost out")
                .price(new BigDecimal("9.99"))
                .category(ProductCategory.GENERAL)
                .stockQuantity(2)
                .available(true)
                .build();
        productRepository.save(lowStockProduct);

        List<Product> products = productRepository.findByStockQuantityGreaterThan(5);

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Test Product");
    }

}
