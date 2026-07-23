package br.com.servix.billing.domain;

public enum FinancialStatus {
    PENDING,
    PARTIALLY_PAID,
    PAID,
    CANCELLED,
    OVERDUE,
    REFUNDED
}
