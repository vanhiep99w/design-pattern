package com.designpatterns.showcase.mvc.mapper;

import com.designpatterns.showcase.common.domain.Order;
import com.designpatterns.showcase.common.domain.OrderItem;
import com.designpatterns.showcase.mvc.dto.OrderDTO;
import com.designpatterns.showcase.mvc.dto.OrderItemDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }

        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .items(order.getItems().stream()
                        .map(this::toItemDTO)
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public OrderItemDTO toItemDTO(OrderItem item) {
        if (item == null) {
            return null;
        }

        return OrderItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

}
