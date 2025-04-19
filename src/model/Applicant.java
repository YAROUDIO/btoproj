package model;

import common.UserRole;

public class Applicant extends User {

    public Applicant(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
    }

    @Override
    public UserRole getRole() {
        return UserRole.APPLICANT;
    }
    public String getPasswordForStorage() {
        return super.getPasswordForStorage();
    }
    public String getName() {
    	return super.getName();
    }
}