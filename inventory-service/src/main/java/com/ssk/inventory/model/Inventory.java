package com.ssk.inventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "t_inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String skuCode;
    
    private Integer quantity;

    // ⚠️ Mandatory no-args constructor for Hibernate proxying
    public Inventory() {}

    public Inventory(Long id, String skuCode, Integer quantity) {
        this.id = id;
        this.skuCode = skuCode;
        this.quantity = quantity;
    }

    // Standard Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    // Legacy record-style getter style helper (keeps your controller running unchanged!)
    public String skuCode() { return skuCode; }
    public Integer quantity() { return quantity; }
}
