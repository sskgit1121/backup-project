package com.ssk.order.service;

import com.ssk.order.client.ProductClient;
import com.ssk.order.client.InventoryClient;
import com.ssk.order.model.Order;
import com.ssk.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class OrderService {

    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final OrderRepository orderRepository; // <-- 1. Inject the Repository

    // Spring auto-injects all three dependencies via this constructor
    public OrderService(ProductClient productClient, 
                        InventoryClient inventoryClient, 
                        OrderRepository orderRepository) {
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
        this.orderRepository = orderRepository;
    }

    public String placeOrder(String productId, String skuCode, int quantity) {
        // 1. Talk to inventory-service to check stock
        boolean isStockAvailable = inventoryClient.isInStock(skuCode, quantity);
        if (!isStockAvailable) {
            return "Order failed: Item is out of stock!";
        }

        // 2. Talk to product-service to get live price metadata
        var product = productClient.getProductById(productId);
        double totalCost = product.price() * quantity;

        // 3. Create the Order Document object
        Order order = new Order(
            null, // MongoDB will auto-generate the document ObjectId
            UUID.randomUUID().toString(), // Generate a unique tracking order number
            productId,
            skuCode,
            quantity,
            totalCost
        );

        // 4. Save the order permanently to your MongoDB container
        Order savedOrder = orderRepository.save(order);
        
        return "Order placed successfully! Order Number: " + savedOrder.orderNumber() + " | Total cost: ₹" + totalCost;
    }
}
