package com.ssk.product.config;

import com.ssk.context.TenantContext;
import org.bson.Document;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;

@Configuration
public class MongoTenantInterceptor extends AbstractMongoEventListener<Object> {

    @Override
    public void onBeforeSave(BeforeSaveEvent<Object> event) {
        Document doc = event.getDocument();
        if (doc != null) {
            String activeTenant = TenantContext.getTenantId();
            if (activeTenant == null) {
                activeTenant = "DEFAULT_SYSTEM_TENANT";
            }
            doc.put("tenant_id", activeTenant);
        }
    }
}
