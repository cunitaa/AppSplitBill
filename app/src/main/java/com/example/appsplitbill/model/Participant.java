package com.example.appsplitbill.model;

import java.io.Serializable;

public class Participant implements Serializable {
    private String userId;
    private String name;
    private String imageUri;
    private double amountOwed;
    private boolean isPaid;

    public Participant() {}

    public Participant(String userId, String name, double amountOwed) {
        this.userId = userId;
        this.name = name;
        this.amountOwed = amountOwed;
        this.isPaid = false;
    }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getAmountOwed() { return amountOwed; }
    public void setAmountOwed(double amountOwed) { this.amountOwed = amountOwed; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
}
