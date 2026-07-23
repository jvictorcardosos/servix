package br.com.servix.billing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "servix.financial")
public class FinancialProperties {

    private boolean autoGenerateOnServiceOrderCompletion = true;

    public boolean isAutoGenerateOnServiceOrderCompletion() {
        return autoGenerateOnServiceOrderCompletion;
    }

    public void setAutoGenerateOnServiceOrderCompletion(boolean autoGenerateOnServiceOrderCompletion) {
        this.autoGenerateOnServiceOrderCompletion = autoGenerateOnServiceOrderCompletion;
    }
}
