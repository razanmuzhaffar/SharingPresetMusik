package com.sharingpresetmusik.model;

public class Akun {
    private String username;
    private String email;
    private String password;
    private int id_user;

    public Akun() {

    }

    public Akun(String username, String password, String email, int id_user) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.id_user = id_user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }
}
