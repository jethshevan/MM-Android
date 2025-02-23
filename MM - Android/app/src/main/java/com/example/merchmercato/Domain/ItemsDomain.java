package com.example.merchmercato.Domain;

import java.io.Serializable;
import java.util.ArrayList;

public class ItemsDomain implements Serializable {
    private String title;
    private String description;
    private ArrayList<String> picUrl;
    private double price;
    private double oldprice;
    private int review;
    private double rating;
    private int NumberinCart;


    public ItemsDomain() {
    }

    public ItemsDomain(String title, String description, ArrayList<String> picurl, double price, double oldprice, int review, double rating) {
        this.title = title;
        this.description = description;
        this.picUrl = picurl;
        this.price = price;
        this.oldprice = oldprice;
        this.review = review;
        this.rating = rating;
    }

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public ArrayList<String> getPicUrl() {return picUrl;}

    public void setPicUrl(ArrayList<String> picurl) {this.picUrl = picurl;}

    public double getPrice() {return price;}

    public void setPrice(double price) {this.price = price;}

    public double getOldprice() {return oldprice;}

    public void setOldprice(double oldprice) {this.oldprice = oldprice;}

    public int getReview() {return review;}

    public void setReview(int review) {this.review = review;}

    public double getRating() {return rating;}

    public void setRating(double rating) {this.rating = rating;}

    public int getNumberinCart() {return NumberinCart;}

    public void setNumberinCart(int numberinCart) {
        NumberinCart = numberinCart;}

    public boolean getQuantity() {
        return false;
    }

    public boolean getName() {
        return false;
    }
}
