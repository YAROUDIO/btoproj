package common;

public enum ApplicationStatus {
    PENDING("PENDING"),
    SUCCESSFUL("SUCCESSFUL"),
    UNSUCCESSFUL("UNSUCCESSFUL"),
    BOOKED("BOOKED");

    private final String status;

    ApplicationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}