package com.example.viktorjankov.shuttletracker.model;

import java.io.Serializable;

public class User implements Serializable {

    // Gotten from registration page
    private String companyCode;
    private String email;
    private String firstName;
    private String lastName;

    public User () {
    }

    public User(String companyCode, String email, String firstName, String lastName) {
        this.companyCode = companyCode;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String toString() {
        return "\n" +
                "company code: " + companyCode + "\n" +
                "email: " + email + "\n" +
                "first: " + firstName + "\n" +
                "last: " + lastName + "\n";
    }
}

