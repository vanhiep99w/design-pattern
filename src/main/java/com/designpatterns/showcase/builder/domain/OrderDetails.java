package com.designpatterns.showcase.builder.domain;

import com.designpatterns.showcase.common.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class OrderDetails {

    private final Long userId;
    private final List<OrderItemDetails> items;
    private final BigDecimal totalAmount;
    private final OrderStatus status;
    private final String notes;
    private final String shippingAddress;
    private final String paymentMethod;
    private final LocalDateTime estimatedDelivery;

    public OrderDetails(Long userId,
                        List<OrderItemDetails> items,
                        BigDecimal totalAmount,
                        OrderStatus status,
                        String notes,
                        String shippingAddress,
                        String paymentMethod,
                        LocalDateTime estimatedDelivery) {
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.notes = notes;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.estimatedDelivery = estimatedDelivery;
    }

    public Long getUserId() {
        return userId;
    }

    public List<OrderItemDetails> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getEstimatedDelivery() {
        return estimatedDelivery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDetails that = (OrderDetails) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(items, that.items) &&
                Objects.equals(totalAmount, that.totalAmount) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, items, totalAmount, status);
    }

    @Override
    public String toString() {
        return "OrderDetails{" +
                "userId=" + userId +
                ", items=" + items.size() +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
}
