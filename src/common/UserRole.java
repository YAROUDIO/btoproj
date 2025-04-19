// File: common/UserRole.java
package common;

public enum UserRole {
    APPLICANT("Applicant"),
    HDB_OFFICER("HDB Officer"),
    HDB_MANAGER("HDB Manager");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}