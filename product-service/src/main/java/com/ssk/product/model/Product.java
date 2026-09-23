package com.ssk.product.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "products")
@CompoundIndex(name = "idx_tenant_sku", def = "{'tenant_id': 1, 'sku_code': 1}", unique = true)
public class Product {

    @Id
    private String id;

    @Field("tenant_id")
    private String tenantId;

    @Field("sku_code")
    private String skuCode;

    private String name;
    private Long price; // Tracked as units/cents

    public Product() {}

    public Product(String skuCode, String name, Long price) {
        this.skuCode = skuCode;
        this.name = name;
        this.price = price;
    }

    public String getId() { return id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
}
