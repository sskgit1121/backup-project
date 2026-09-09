package com.ssk.order.client;

import java.math.BigDecimal;

public record ProductResponse(String id, String name, BigDecimal price, String description) {}
