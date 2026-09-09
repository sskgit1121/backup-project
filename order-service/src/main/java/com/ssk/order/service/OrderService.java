package com.ssk.order.service;

import com.ssk.order.model.Order;
import com.ssk.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public String placeOrder(String productId, String skuCode, int quantity) {
        // 1. In a later step, we will call inventory-service here to verify stock!
        
        // 2. Map input payload parameters to our persistent relational Order model
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString()); // Auto-generates unique tracking identifier
        order.setSkuCode(skuCode);
        order.setQuantity(quantity);
        
        // Mock lookup price mapping for toy checkout simulation (e.g., 99.99 per unit)
        order.setPrice(BigDecimal.valueOf(99.99).multiply(BigDecimal.valueOf(quantity)));

        // 3. Persist the transaction details securely into PostgreSQL
        orderRepository.save(order);
        
        return "Order placed successfully! Transaction Tracking Number: " + order.getOrderNumber();
    }
}
