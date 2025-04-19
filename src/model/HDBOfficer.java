package model;

import common.UserRole;

public class HDBOfficer extends User {

    public HDBOfficer(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
    }

    @Override
    public UserRole getRole() {
        return UserRole.HDB_OFFICER;
    }
    
    public String getPasswordForStorage() {
        return super.getPasswordForStorage();
    }
}
