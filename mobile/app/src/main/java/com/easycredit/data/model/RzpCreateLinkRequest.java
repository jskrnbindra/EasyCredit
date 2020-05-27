package com.easycredit.data.model;

public class RzpCreateLinkRequest {

    private final String type = "link";

    private int amount;
    private String description;
    private RzpCustomer customer;

    public RzpCreateLinkRequest(int amount, String description, RzpCustomer customer) {
        this.amount = amount;
        this.description = description;
        this.customer = customer;
    }

    public RzpCustomer getCustomer() {
        return customer;
    }

    public String getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCustomer(RzpCustomer customer) {
        this.customer = customer;
    }
}
