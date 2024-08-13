package com.example.wheaton_market;

import java.util.ArrayList;

public class Item {
    private String itemId;
    private String name;
    private String description;
    private double price;
    private String imageURL;

    public Item() {

    }

    public Item(String itemId, String name, String description, double price, String imageURL) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageURL = imageURL;
    }

    // Getters and setters for Firebase serialization
    public String getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

}
