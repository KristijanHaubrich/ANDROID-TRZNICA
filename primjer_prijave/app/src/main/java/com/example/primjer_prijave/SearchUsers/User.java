package com.example.primjer_prijave.SearchUsers;

public class User {

    private String username, email;

    public User(){};

    public User(String username, String email){
        this.username = username;
        this.email = email;

    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}