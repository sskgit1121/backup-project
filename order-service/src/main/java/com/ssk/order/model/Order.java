package com.ssk.order.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")
public record Order(@Id String id, String orderNumber, String productId, String skuCode, int quantity, double totalCost) {}
