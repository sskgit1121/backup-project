package com.ssk.order.client;

// A Java Record is a clean way to define immutable DTO data carriers
public record ProductResponse(
    String id, 
    String name, 
    double price
) {}
