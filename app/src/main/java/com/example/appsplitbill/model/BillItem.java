package com.example.appsplitbill.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BillItem implements Serializable {
    private String name;
    private double price;
    private int quantity = 1;
    private List<String> consumerNames = new ArrayList<>();

    public BillItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public BillItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public List<String> getConsumerNames() { return consumerNames; }
    public void setConsumerNames(List<String> consumerNames) { this.consumerNames = consumerNames; }
}
