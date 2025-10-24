package com.designpatterns.showcase.mvc.dto;

import com.designpatterns.showcase.common.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @Builder.Default
    private List<OrderItemDTO> items = new ArrayList<>();

    private BigDecimal totalAmount;

    private OrderStatus status;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
