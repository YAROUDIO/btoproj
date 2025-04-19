package common;

public enum RegistrationStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private final String status;

    RegistrationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}