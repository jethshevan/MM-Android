package com.example.merchmercato.Helper;


public class User {
    private String address; // Ensure this is a String
    private String postalCode; // Ensure this is a String
    private int dob; // If this is an int, handle accordingly

    // Add your constructors, getters, and setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public int getDob() {
        return dob;
    }

    public void setDob(int dob) {
        this.dob = dob;
    }
}
