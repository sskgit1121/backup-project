
package com.ssk.inventory.model;

import com.ssk.context.TenantContext;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "t_inventory")
@EntityListeners(AuditingEntityListener.class)
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(
                name = "tenantId",
                type = String.class
        )
)
@Filter(
        name = "tenantFilter",
        condition = "tenant_id = :tenantId"
)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "tenant_id",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String tenantId;

    @Column(
            name = "sku_code",
            nullable = false,
            length = 100
    )
    private String skuCode;

    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity;
    
// --- AUDITING COLUMNS ---
    
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
    
//    // --- REQUIRED BY PERSISTABLE INTERFACE ---
//    @Override
//    @Transient // Prevents Hibernate from treating this method as a database column
//    public boolean isNew() {
//        // If the ID is null, it's a brand new record insert -> triggers @CreatedBy
//        return this.id == null;
//    }

    @PrePersist
    public void autoAssignTenantContext() {
        if (this.tenantId == null || this.tenantId.isBlank()) {
            String currentTenant = TenantContext.getTenantId();
            if (currentTenant == null || currentTenant.isBlank()) {
                throw new IllegalStateException("Cannot create inventory without an active tenant context");
            }
            this.tenantId = currentTenant;
        }
        validateQuantity();
    }

    @PreUpdate
    public void validateBeforeUpdate() {
        // Explicitly block any cross-tenant database updates at the application firewall layer
        String currentActiveTenant = TenantContext.getTenantId();
        if (this.tenantId == null || !this.tenantId.equals(currentActiveTenant)) {
            throw new IllegalStateException(
                "Security Violation: Cross-tenant data mutation detected!"
            );
        }
        
        validateQuantity();
    }


    private void validateQuantity() {

        if (quantity == null) {
            throw new IllegalArgumentException(
                    "Inventory quantity cannot be null"
            );
        }

        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Inventory quantity cannot be negative"
            );
        }
    }

    public Long getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedOn() { return createdOn; }
    public String getUpdatedBy() { return updatedBy; }
    public LocalDateTime getUpdatedOn() { return updatedOn; }
}

