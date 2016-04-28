package com.ivigilate.android.library.classes;

public class User {
    public int id;
    public String company_id;
    public String email;
    public String password;
    public String first_name;
    public String last_name;
    public String metadata;
    public String token;

    public User() {}

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
