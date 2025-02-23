package com.example.merchmercato.Helper;


public class CartItem {
    private String name;
    private int quantity;
    private double price;

    // Constructor
    public CartItem(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}
