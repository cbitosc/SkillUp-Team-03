package com.example.cosc_project;

public class User {
    private String username,password;
    private String name;
    private String branch;
    private String sem;
    public User(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }

    public User(String username, String password,String branch,String sem) {
        this.username = username;
        this.password = password;
        this.branch=branch;
        this.sem=sem;
    }


    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getBranch() {
        return branch;
    }

    public String getSem() {
        return sem;
    }

}
