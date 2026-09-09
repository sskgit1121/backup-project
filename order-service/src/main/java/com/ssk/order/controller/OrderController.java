package com.ssk.order.controller;


import com.ssk.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    // Spring auto-injects your OrderService business logic bean
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
        String result = orderService.placeOrder(
            orderRequest.productId(), 
            orderRequest.skuCode(), 
            orderRequest.quantity()
        );
        
        if (result.contains("failed")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    // 2. ADD THIS NEW METHOD to handle GET requests for /api/orders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<String> getOrderById(@PathVariable String id) {
        // Replace this with your actual database lookup logic from OrderService
        // Example: Order order = orderService.getOrderById(id);
        
        String mockResponse = "Fetched order details for ID: " + id;
        return ResponseEntity.ok(mockResponse);
    }
}

// Simple request payload schema
record OrderRequest(String productId, String skuCode, int quantity) {}

