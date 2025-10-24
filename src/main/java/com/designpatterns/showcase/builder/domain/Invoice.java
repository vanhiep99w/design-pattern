package com.designpatterns.showcase.builder.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Invoice {

    private final String invoiceNumber;
    private final String customerName;
    private final String customerEmail;
    private final String customerAddress;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private final List<InvoiceItem> items;
    private final BigDecimal subtotal;
    private final BigDecimal taxRate;
    private final BigDecimal taxAmount;
    private final BigDecimal discountAmount;
    private final BigDecimal totalAmount;
    private final String notes;
    private final String paymentTerms;

    private Invoice(Builder builder) {
        this.invoiceNumber = builder.invoiceNumber;
        this.customerName = builder.customerName;
        this.customerEmail = builder.customerEmail;
        this.customerAddress = builder.customerAddress;
        this.issueDate = builder.issueDate;
        this.dueDate = builder.dueDate;
        this.items = Collections.unmodifiableList(builder.items);
        this.subtotal = builder.subtotal;
        this.taxRate = builder.taxRate;
        this.taxAmount = builder.taxAmount;
        this.discountAmount = builder.discountAmount;
        this.totalAmount = builder.totalAmount;
        this.notes = builder.notes;
        this.paymentTerms = builder.paymentTerms;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(invoiceNumber, invoice.invoiceNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceNumber);
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceNumber='" + invoiceNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", issueDate=" + issueDate +
                ", dueDate=" + dueDate +
                '}';
    }

    public static final class Builder {
        private String invoiceNumber;
        private String customerName;
        private String customerEmail;
        private String customerAddress;
        private LocalDate issueDate;
        private LocalDate dueDate;
        private List<InvoiceItem> items;
        private BigDecimal subtotal;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private String notes;
        private String paymentTerms;

        private Builder() {
            this.issueDate = LocalDate.now();
            this.dueDate = LocalDate.now().plusDays(30);
            this.taxRate = BigDecimal.ZERO;
            this.discountAmount = BigDecimal.ZERO;
            this.paymentTerms = "Net 30";
        }

        public Builder invoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
            return this;
        }

        public Builder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public Builder customerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
            return this;
        }

        public Builder customerAddress(String customerAddress) {
            this.customerAddress = customerAddress;
            return this;
        }

        public Builder issueDate(LocalDate issueDate) {
            this.issueDate = issueDate;
            return this;
        }

        public Builder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder items(List<InvoiceItem> items) {
            this.items = items;
            return this;
        }

        public Builder taxRate(BigDecimal taxRate) {
            this.taxRate = taxRate;
            return this;
        }

        public Builder discountAmount(BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder paymentTerms(String paymentTerms) {
            this.paymentTerms = paymentTerms;
            return this;
        }

        public Invoice build() {
            validate();
            calculateAmounts();
            return new Invoice(this);
        }

        private void validate() {
            if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
                throw new IllegalStateException("Invoice number is required");
            }
            if (customerName == null || customerName.trim().isEmpty()) {
                throw new IllegalStateException("Customer name is required");
            }
            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                throw new IllegalStateException("Customer email is required");
            }
            if (!customerEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new IllegalStateException("Customer email is invalid");
            }
            if (items == null || items.isEmpty()) {
                throw new IllegalStateException("Invoice must have at least one item");
            }
            if (issueDate == null) {
                throw new IllegalStateException("Issue date is required");
            }
            if (dueDate == null) {
                throw new IllegalStateException("Due date is required");
            }
            if (dueDate.isBefore(issueDate)) {
                throw new IllegalStateException("Due date cannot be before issue date");
            }
            if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("Tax rate cannot be negative");
            }
            if (taxRate != null && taxRate.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalStateException("Tax rate cannot be greater than 100%");
            }
            if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("Discount amount cannot be negative");
            }
        }

        private void calculateAmounts() {
            this.subtotal = items.stream()
                    .map(InvoiceItem::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (discountAmount != null && discountAmount.compareTo(subtotal) > 0) {
                throw new IllegalStateException("Discount amount cannot be greater than subtotal");
            }

            BigDecimal amountAfterDiscount = subtotal.subtract(discountAmount);
            this.taxAmount = amountAfterDiscount.multiply(taxRate);
            this.totalAmount = amountAfterDiscount.add(taxAmount);
        }
    }
}
