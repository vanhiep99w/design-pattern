package com.designpatterns.showcase.builder.manual;

import com.designpatterns.showcase.builder.domain.OrderDetails;
import com.designpatterns.showcase.builder.domain.OrderItemDetails;
import com.designpatterns.showcase.common.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderBuilder {

    private Long userId;
    private List<OrderItemDetails> items;
    private OrderStatus status;
    private String notes;
    private String shippingAddress;
    private String paymentMethod;
    private LocalDateTime estimatedDelivery;

    private OrderBuilder() {
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
    }

    public static OrderBuilder newOrder() {
        return new OrderBuilder();
    }

    public OrderBuilder forUser(Long userId) {
        this.userId = userId;
        return this;
    }

    public OrderBuilder addItem(String productName, int quantity, BigDecimal unitPrice) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.items.add(new OrderItemDetails(productName, quantity, unitPrice, subtotal));
        return this;
    }

    public OrderBuilder addItem(OrderItemDetails item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        this.items.add(item);
        return this;
    }

    public OrderBuilder withStatus(OrderStatus status) {
        this.status = status;
        return this;
    }

    public OrderBuilder withNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public OrderBuilder shippingTo(String address) {
        this.shippingAddress = address;
        return this;
    }

    public OrderBuilder payingWith(String paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public OrderBuilder estimatedDelivery(LocalDateTime estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
        return this;
    }

    public OrderDetails build() {
        validate();
        BigDecimal totalAmount = calculateTotalAmount();
        return new OrderDetails(
                userId,
                Collections.unmodifiableList(new ArrayList<>(items)),
                totalAmount,
                status,
                notes,
                shippingAddress,
                paymentMethod,
                estimatedDelivery
        );
    }

    private void validate() {
        if (userId == null) {
            throw new IllegalStateException("User ID is required");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Order must have at least one item");
        }
        if (status == null) {
            throw new IllegalStateException("Order status is required");
        }
    }

    private BigDecimal calculateTotalAmount() {
        return items.stream()
                .map(OrderItemDetails::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public OrderBuilder clear() {
        this.items.clear();
        return this;
    }

    public int getItemCount() {
        return items.size();
    }

    public BigDecimal getCurrentTotal() {
        return calculateTotalAmount();
    }
}
