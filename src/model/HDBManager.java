package model;

import common.UserRole;

public class HDBManager extends User {

    public HDBManager(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
    }

    @Override
    public UserRole getRole() {
        return UserRole.HDB_MANAGER;
    }
    public String getPasswordForStorage() {
        return super.getPasswordForStorage();
    }
}
