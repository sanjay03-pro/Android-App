package com.may25.app.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private boolean online;

    public User() {}

    public User(String uid, String name, String email) {
        this.uid    = uid;
        this.name   = name;
        this.email  = email;
        this.online = false;
    }

    public String getUid()    { return uid; }
    public String getName()   { return name; }
    public String getEmail()  { return email; }
    public boolean isOnline() { return online; }

    public void setUid(String uid)      { this.uid = uid; }
    public void setName(String name)    { this.name = name; }
    public void setEmail(String email)  { this.email = email; }
    public void setOnline(boolean online) { this.online = online; }
}
