package com.designpatterns.showcase.mvc.service;

import com.designpatterns.showcase.common.domain.*;
import com.designpatterns.showcase.common.repository.ProductRepository;
import com.designpatterns.showcase.common.repository.UserRepository;
import com.designpatterns.showcase.mvc.dto.OrderDTO;
import com.designpatterns.showcase.mvc.dto.OrderItemDTO;
import com.designpatterns.showcase.mvc.exception.InvalidRequestException;
import com.designpatterns.showcase.mvc.exception.ResourceNotFoundException;
import com.designpatterns.showcase.mvc.mapper.OrderMapper;
import com.designpatterns.showcase.mvc.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        log.debug("Fetching all orders");
        return orderRepository.findAll().stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        log.debug("Fetching order with id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return orderMapper.toDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        log.debug("Fetching orders for user id: {}", userId);
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        log.debug("Fetching orders by status: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO createOrder(OrderDTO orderDTO) {
        log.debug("Creating new order for user id: {}", orderDTO.getUserId());
        
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", orderDTO.getUserId()));
        
        if (orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            throw new InvalidRequestException("Order must contain at least one item");
        }
        
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .notes(orderDTO.getNotes())
                .build();
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderItemDTO itemDTO : orderDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemDTO.getProductId()));
            
            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                throw new InvalidRequestException("Insufficient stock for product: " + product.getName());
            }
            
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();
            
            order.addItem(orderItem);
            totalAmount = totalAmount.add(subtotal);
            
            product.setStockQuantity(product.getStockQuantity() - itemDTO.getQuantity());
            productRepository.save(product);
        }
        
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with id: {}", savedOrder.getId());
        return orderMapper.toDTO(savedOrder);
    }

    public OrderDTO updateOrderStatus(Long id, OrderStatus status) {
        log.debug("Updating order status for id: {} to {}", id, status);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated for id: {}", updatedOrder.getId());
        return orderMapper.toDTO(updatedOrder);
    }

    public void deleteOrder(Long id) {
        log.debug("Deleting order with id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
        
        orderRepository.deleteById(id);
        log.info("Order deleted with id: {}", id);
    }

}
