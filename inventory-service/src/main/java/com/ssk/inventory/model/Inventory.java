package com.ssk.inventory.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "inventory")
public record Inventory(@Id String id, String skuCode, int quantity) {}

