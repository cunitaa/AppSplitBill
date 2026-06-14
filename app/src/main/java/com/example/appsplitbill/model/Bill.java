package com.example.appsplitbill.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Bill implements Serializable {
    private String id;
    private String title;
    private String date;
    private double totalAmount;
    private String status; // "PENDING" or "LUNAS"
    private String creatorId;
    private String payerName; // Who paid the bill
    private double taxPercent;
    private double servicePercent;
    private double discountAmount;
    private List<String> peopleResults = new ArrayList<>();
    private List<Boolean> paidStatus = new ArrayList<>();

    public Bill() {}

    public String getPayerName() { return payerName; }
    public void setPayerName(String payerName) { this.payerName = payerName; }
    public double getTaxPercent() { return taxPercent; }
    public void setTaxPercent(double taxPercent) { this.taxPercent = taxPercent; }
    public double getServicePercent() { return servicePercent; }
    public void setServicePercent(double servicePercent) { this.servicePercent = servicePercent; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public Bill(String title, String date, double totalAmount, String status, String creatorId) {
        this.title = title;
        this.date = date;
        this.totalAmount = totalAmount;
        this.status = status;
        this.creatorId = creatorId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public List<String> getPeopleResults() { return peopleResults; }
    public void setPeopleResults(List<String> peopleResults) {
        this.peopleResults = peopleResults;
        if (paidStatus == null || paidStatus.isEmpty()) {
            paidStatus = new ArrayList<>();
            for (int i = 0; i < peopleResults.size(); i++) paidStatus.add(false);
        }
    }

    public List<Boolean> getPaidStatus() { return paidStatus; }
    public void setPaidStatus(List<Boolean> paidStatus) { this.paidStatus = paidStatus; }
}
