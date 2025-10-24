package com.designpatterns.showcase.strategy.context;

import com.designpatterns.showcase.strategy.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    private final DiscountContext discountContext;
    private final ShippingContext shippingContext;

    public PricingResponse calculateTotalPrice(PricingRequest request) {
        log.info("Calculating total price for customer: {}", request.getCustomerId());

        DiscountRequest discountRequest = DiscountRequest.builder()
                .customerId(request.getCustomerId())
                .orderAmount(request.getOrderAmount())
                .customerTier(request.getCustomerTier())
                .orderDate(request.getOrderDate())
                .productCategory(request.getProductCategory())
                .itemCount(request.getItemCount())
                .isFirstOrder(request.isFirstOrder())
                .build();

        DiscountResult discountResult;
        if (request.getDiscountStrategy() != null && !request.getDiscountStrategy().isEmpty()) {
            discountResult = discountContext.applyDiscount(request.getDiscountStrategy(), discountRequest);
        } else {
            discountResult = discountContext.applyBestDiscount(discountRequest);
        }

        ShippingRequest shippingRequest = ShippingRequest.builder()
                .destinationCountry(request.getDestinationCountry())
                .destinationZipCode(request.getDestinationZipCode())
                .packageWeight(request.getPackageWeight())
                .packageVolume(request.getPackageVolume())
                .isFragile(request.isFragile())
                .requiresSignature(request.isRequiresSignature())
                .build();

        ShippingCostResult shippingResult;
        if (request.getShippingStrategy() != null && !request.getShippingStrategy().isEmpty()) {
            shippingResult = shippingContext.calculateShippingCost(
                    request.getShippingStrategy(),
                    shippingRequest);
        } else {
            shippingResult = shippingContext.calculateShippingCostAuto(shippingRequest);
        }

        BigDecimal totalAmount = discountResult.getFinalAmount().add(shippingResult.getTotalCost());

        log.info("Total price calculated: ${} (Subtotal: ${}, Discount: ${}, Shipping: ${})",
                totalAmount,
                request.getOrderAmount(),
                discountResult.getDiscountAmount(),
                shippingResult.getTotalCost());

        return PricingResponse.builder()
                .subtotal(request.getOrderAmount())
                .discountAmount(discountResult.getDiscountAmount())
                .shippingCost(shippingResult.getTotalCost())
                .totalAmount(totalAmount)
                .discountDescription(discountResult.getDescription())
                .shippingDescription(shippingResult.getDescription())
                .estimatedDeliveryDays(shippingResult.getEstimatedDeliveryDays())
                .build();
    }
}
