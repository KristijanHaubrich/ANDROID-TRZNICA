package com.example.primjer_prijave.AddProduct;

public class Product {

    private String description,name,price, imageUri;

    Product(){}

    public Product(String description, String name, String price, String imageUri){
        this.description = description;
        this.name = name;
        this.price = price;
        this.imageUri = imageUri;
    };

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
