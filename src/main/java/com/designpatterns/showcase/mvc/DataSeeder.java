package com.designpatterns.showcase.mvc;

import com.designpatterns.showcase.common.domain.*;
import com.designpatterns.showcase.common.repository.ProductRepository;
import com.designpatterns.showcase.common.repository.UserRepository;
import com.designpatterns.showcase.mvc.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    @Bean
    CommandLineRunner seedMvcData(
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository) {
        return args -> {
            log.info("Seeding MVC showcase data...");

            if (userRepository.count() == 0) {
                List<User> users = List.of(
                        User.builder()
                                .username("john.doe")
                                .email("john.doe@example.com")
                                .firstName("John")
                                .lastName("Doe")
                                .role(UserRole.ADMIN)
                                .active(true)
                                .build(),
                        User.builder()
                                .username("jane.smith")
                                .email("jane.smith@example.com")
                                .firstName("Jane")
                                .lastName("Smith")
                                .role(UserRole.USER)
                                .active(true)
                                .build(),
                        User.builder()
                                .username("bob.wilson")
                                .email("bob.wilson@example.com")
                                .firstName("Bob")
                                .lastName("Wilson")
                                .role(UserRole.USER)
                                .active(true)
                                .build()
                );
                userRepository.saveAll(users);
                log.info("Seeded {} users", users.size());
            }

            if (productRepository.count() == 0) {
                List<Product> products = List.of(
                        Product.builder()
                                .name("Laptop")
                                .description("High-performance laptop with 16GB RAM")
                                .price(new BigDecimal("1299.99"))
                                .category(ProductCategory.ELECTRONICS)
                                .stockQuantity(50)
                                .available(true)
                                .build(),
                        Product.builder()
                                .name("Smartphone")
                                .description("Latest model smartphone with 5G")
                                .price(new BigDecimal("899.99"))
                                .category(ProductCategory.ELECTRONICS)
                                .stockQuantity(100)
                                .available(true)
                                .build(),
                        Product.builder()
                                .name("T-Shirt")
                                .description("Cotton t-shirt, available in multiple colors")
                                .price(new BigDecimal("19.99"))
                                .category(ProductCategory.CLOTHING)
                                .stockQuantity(200)
                                .available(true)
                                .build(),
                        Product.builder()
                                .name("Jeans")
                                .description("Denim jeans, comfortable fit")
                                .price(new BigDecimal("49.99"))
                                .category(ProductCategory.CLOTHING)
                                .stockQuantity(150)
                                .available(true)
                                .build(),
                        Product.builder()
                                .name("Java Programming Book")
                                .description("Comprehensive guide to Java programming")
                                .price(new BigDecimal("39.99"))
                                .category(ProductCategory.BOOKS)
                                .stockQuantity(75)
                                .available(true)
                                .build(),
                        Product.builder()
                                .name("Coffee Beans")
                                .description("Premium Arabica coffee beans, 1kg")
                                .price(new BigDecimal("24.99"))
                                .category(ProductCategory.FOOD)
                                .stockQuantity(30)
                                .available(true)
                                .build()
                );
                productRepository.saveAll(products);
                log.info("Seeded {} products", products.size());
            }

            if (orderRepository.count() == 0) {
                User user = userRepository.findByUsername("jane.smith").orElse(null);
                Product laptop = productRepository.findByNameContainingIgnoreCase("Laptop").get(0);
                Product book = productRepository.findByNameContainingIgnoreCase("Java").get(0);

                if (user != null && laptop != null && book != null) {
                    Order order = Order.builder()
                            .user(user)
                            .status(OrderStatus.CONFIRMED)
                            .notes("First order - expedite shipping")
                            .build();

                    OrderItem item1 = OrderItem.builder()
                            .product(laptop)
                            .quantity(1)
                            .unitPrice(laptop.getPrice())
                            .subtotal(laptop.getPrice())
                            .build();

                    OrderItem item2 = OrderItem.builder()
                            .product(book)
                            .quantity(2)
                            .unitPrice(book.getPrice())
                            .subtotal(book.getPrice().multiply(BigDecimal.valueOf(2)))
                            .build();

                    order.addItem(item1);
                    order.addItem(item2);

                    BigDecimal total = item1.getSubtotal().add(item2.getSubtotal());
                    order.setTotalAmount(total);

                    orderRepository.save(order);
                    log.info("Seeded 1 sample order");
                }
            }

            log.info("MVC showcase data seeding completed");
        };
    }

}
