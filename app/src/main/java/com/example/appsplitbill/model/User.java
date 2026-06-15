package com.example.appsplitbill.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String id;
    private String name;
    private String email;
    private String password;
    private String photoUrl;
    private String whatsapp;
    private String bankInfo;
    private List<String> friends = new ArrayList<>();

    public User() {}

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
    public String getBankInfo() { return bankInfo; }
    public void setBankInfo(String bankInfo) { this.bankInfo = bankInfo; }
    public List<String> getFriends() { return friends; }
    public void setFriends(List<String> friends) { this.friends = friends; }
}
