package com.designpatterns.showcase.builder.domain;

import java.math.BigDecimal;
import java.util.Objects;

public final class InvoiceItem {

    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal total;

    private InvoiceItem(Builder builder) {
        this.description = builder.description;
        this.quantity = builder.quantity;
        this.unitPrice = builder.unitPrice;
        this.total = builder.unitPrice.multiply(BigDecimal.valueOf(builder.quantity));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotal() {
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceItem that = (InvoiceItem) o;
        return quantity == that.quantity &&
                Objects.equals(description, that.description) &&
                Objects.equals(unitPrice, that.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, quantity, unitPrice);
    }

    @Override
    public String toString() {
        return "InvoiceItem{" +
                "description='" + description + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", total=" + total +
                '}';
    }

    public static final class Builder {
        private String description;
        private int quantity;
        private BigDecimal unitPrice;

        private Builder() {
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder unitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public InvoiceItem build() {
            validate();
            return new InvoiceItem(this);
        }

        private void validate() {
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalStateException("Item description is required");
            }
            if (quantity <= 0) {
                throw new IllegalStateException("Item quantity must be positive");
            }
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("Item unit price must be positive");
            }
        }
    }
}
