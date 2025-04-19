package common;

public enum FilePath {
    APPLICANT("data/ApplicantList.csv"),
    OFFICER("data/OfficerList.csv"),
    MANAGER("data/ManagerList.csv"),
    PROJECT("data/ProjectList.csv"),
    APPLICATION("data/ApplicationData.csv"),
    REGISTRATION("data/RegistrationData.csv"),
    ENQUIRY("data/EnquiryData.csv");

    private final String path;

    FilePath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
