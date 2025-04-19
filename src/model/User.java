package model;

import common.UserRole;
import exception.OperationError;
import util.InputUtil;

public abstract class User {
    // Fields
    private String name;
    private String nric;
    private int age;
    private String maritalStatus;
    private String password;

    // Constructor
    public User(String name, String nric, int age, String maritalStatus, String password) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (!InputUtil.validateNric(nric)) {
            throw new IllegalArgumentException("Invalid NRIC format: " + nric);
        }
        if (age < 0) {
            throw new IllegalArgumentException("Invalid age value: " + age);
        }
        if (maritalStatus == null || maritalStatus.isEmpty()) {
            throw new IllegalArgumentException("Marital status cannot be empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        this.name = name;
        this.nric = nric;
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.password = password;
    }

    // Getter Methods
    public String getName() {
        return name;
    }

    public String getNric() {
        return nric;
    }

    public int getAge() {
        return age;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    // Password-related Methods
    public boolean checkPassword(String passwordAttempt) {
        return this.password.equals(passwordAttempt);
    }

    public void changePassword(String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new OperationError("Password cannot be empty.");
        }
        // Add more password complexity rules if needed
        this.password = newPassword;
    }

    public String getPasswordForStorage() {
        return this.password;
    }

    // Abstract Method
    public abstract UserRole getRole();

    // Method for Display
    public String getDisplayDetails() {
        return "Name: " + this.name + ", NRIC: " + this.nric + ", Role: " + getRole().getRole();
    }

    // Equality based on NRIC
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return this.nric.equals(user.nric);
    }

    @Override
    public int hashCode() {
        return this.nric.hashCode();
    }
}
