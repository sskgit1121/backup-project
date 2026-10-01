package com.ssk.inventory.dto;

import java.io.Serializable;

public record InventoryEvent(
    String skuCode,
    Integer quantityDeducted,
    String tenantId,      // CRUCIAL: Retains structural logical isolation boundary
    String triggeredBy    // Tracking identity ('clerk@alpha.com')
) implements Serializable {}
