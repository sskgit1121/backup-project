package com.ssk.order.service;

import com.ssk.order.client.InventoryClient;
import com.ssk.order.client.ProductClient;
import com.ssk.order.model.Order;
import com.ssk.order.repository.OrderRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient; // 🔌 Injected Stock Client
    private final ProductClient productClient;     // 🔌 Injected Product Client

    public OrderService(OrderRepository orderRepository, InventoryClient inventoryClient, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.productClient = productClient;
    }
 // 1. Add the Circuit Breaker annotation. Define a unique name and the fallback method name.
    //Add api-gatway in api-config & @Retry on order
    @CircuitBreaker(name = "inventoryServiceBreaker", fallbackMethod = "inventoryFallback")
    @Retry(name = "orderRetry")
    public String placeOrder(String productId, String skuCode, int quantity) {
        
        // 1.  Call NoSQL product-service via OpenFeign to fetch real price details from MongoDB
    	// 🚀 Prefix the type with ProductClient. so the compiler matches it perfectly
    	ProductClient.ProductResponse product = productClient.getProductById(productId);
        if (product == null) {
            throw new RuntimeException("Order Failed: Product code " + productId + " does not exist in Catalog.");
        }

        // 2.  Call SQL inventory-service via OpenFeign to verify live stock levels in PostgreSQL
        boolean isItemInStock = inventoryClient.isInStock(skuCode, quantity);
        
        if (!isItemInStock) {
            throw new RuntimeException("Order Failed: Requested quantity for SKU " + skuCode + " is out of stock!");
        }

        // 3. ✍️ If both validations pass, calculate dynamic bill and persist record to Order PostgreSQL table
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setSkuCode(skuCode);
        order.setQuantity(quantity);
        
        // Calculate dynamic total using the real product price retrieved from MongoDB
        BigDecimal totalCalculatedPrice = product.price().multiply(BigDecimal.valueOf(quantity));
        order.setPrice(totalCalculatedPrice);

        orderRepository.save(order);
        
        return "Success! Order securely placed. Tracking ID: " + order.getOrderNumber() 
                + " | Total Charged: ₹" + totalCalculatedPrice;
    }
    
 // 2. 🛡️ WRITE THE FALLBACK METHOD
    // ⚠️ IMPORTANT: It MUST have the exact same method arguments as placeOrder(), plus a Throwable parameter at the end!
    public String inventoryFallback(String productId, String skuCode, int quantity, Throwable throwable) {
        return "Order Processing Delayed: The Inventory Verification System is temporarily offline. "
             + "Our engineers are fixing it. Please retry your order shortly! (Reason: " + throwable.getMessage() + ")";
    }
}
