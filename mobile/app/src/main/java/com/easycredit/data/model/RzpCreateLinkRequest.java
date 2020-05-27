package com.easycredit.data.model;

public class RzpCreateLinkRequest {

    private final String type = "link";
    private final String callback_url = "https://easycredit.azurewebsites.net/api/paymentLinkCallback";
    private final String callback_method = "get";

    private int amount;
    private String description;
    private String receipt;
    private RzpCustomer customer;

    public RzpCreateLinkRequest(int amount, String description, RzpCustomer customer, String receipt) {
        this.amount = amount;
        this.description = description;
        this.customer = customer;
        this.receipt = receipt;
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

    public String getReceipt() {
        return receipt;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCustomer(RzpCustomer customer) {
        this.customer = customer;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }
}
