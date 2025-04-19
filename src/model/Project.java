package model;

import common.FlatType;
import common.Displayable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import exception.DataLoadError;
import exception.OperationError;
import util.InputUtil;
import util.DateUtil;

public class Project implements Displayable{

    // Headers for CSV consistency
    public static final String[] HEADERS = {
        "Project Name", "Neighborhood", "Type 1", "Number of units for Type 1",
        "Selling price for Type 1", "Type 2", "Number of units for Type 2",
        "Selling price for Type 2", "Application opening date",
        "Application closing date", "Manager", "Officer Slot", "Officer", "Visibility"
    };

    private String projectName;
    private String neighborhood;
    private FlatType type1 = FlatType.TWO_ROOM;
    private int numUnits1;
    private int price1;
    private FlatType type2 = FlatType.THREE_ROOM;
    private int numUnits2;
    private int price2;
    private Date openingDate;
    private Date closingDate;
    private String managerNric;
    private int officerSlot;
    private List<String> officerNrics = new ArrayList<>();
    private boolean visibility;

    // Constructor
    public Project(String projectName, String neighborhood, int numUnits1, int price1, 
                   int numUnits2, int price2, Date openingDate, Date closingDate, 
                   String managerNric, int officerSlot, List<String> officerNrics, boolean visibility) throws IllegalArgumentException {
        if (projectName == null || projectName.isEmpty()) throw new IllegalArgumentException("Project Name cannot be empty");
        if (neighborhood == null || neighborhood.isEmpty()) throw new IllegalArgumentException("Neighborhood cannot be empty");
        if (managerNric == null || managerNric.isEmpty() || !InputUtil.validateNric(managerNric)) {
            throw new IllegalArgumentException("Invalid Manager NRIC: " + managerNric);
        }
        if (openingDate == null || closingDate == null) {
            throw new IllegalArgumentException("Opening and Closing dates must be valid date objects.");
        }
        if (closingDate.before(openingDate)) {
            throw new IllegalArgumentException("Closing date cannot be before opening date.");
        }
        if (officerSlot < 0 || officerSlot > 10) {
            throw new IllegalArgumentException("Officer slots must be between 0 and 10.");
        }
        if (numUnits1 < 0 || price1 < 0 || numUnits2 < 0 || price2 < 0) {
            throw new IllegalArgumentException("Numeric project values (units, price) cannot be negative.");
        }

        this.projectName = projectName;
        this.neighborhood = neighborhood;
        this.numUnits1 = numUnits1;
        this.price1 = price1;
        this.numUnits2 = numUnits2;
        this.price2 = price2;
        this.openingDate = openingDate;
        this.closingDate = closingDate;
        this.managerNric = managerNric;
        this.officerSlot = officerSlot;
        this.officerNrics = officerNrics != null ? officerNrics : new ArrayList<>();
        this.visibility = visibility;

        if (this.officerNrics.size() > this.officerSlot) {
            throw new IllegalArgumentException("Number of assigned officers exceeds available slots.");
        }
    }

    // --- Getters ---
    public String getProjectName() { return projectName; }
    public String getNeighborhood() { return neighborhood; }
    public Date getOpeningDate() { return openingDate; }
    public Date getClosingDate() { return closingDate; }
    public String getManagerNric() { return managerNric; }
    public int getOfficerSlot() { return officerSlot; }
    public boolean isVisibility() { return visibility; }
    public List<String> getOfficerNrics() { return new ArrayList<>(officerNrics); }

    // --- Calculated Properties / State Checks ---
    public boolean isActivePeriod(Date checkDate) {
        if (checkDate == null) checkDate = new Date();
        return !openingDate.after(checkDate) && !closingDate.before(checkDate);
    }

    public boolean isCurrentlyVisibleAndActive() {
        return visibility && isActivePeriod(new Date());
    }

    public int[] getFlatDetails(FlatType flatType) {
        if (flatType == FlatType.TWO_ROOM) {
            return new int[]{numUnits1, price1};
        }
        if (flatType == FlatType.THREE_ROOM) {
            return new int[]{numUnits2, price2};
        }
        throw new IllegalArgumentException("Invalid flat type requested: " + flatType);
    }

    public int getAvailableOfficerSlots() {
        return officerSlot - officerNrics.size();
    }

    public boolean canAddOfficer() {
        return getAvailableOfficerSlots() > 0;
    }

    // --- State Modifiers ---
    public boolean decreaseUnitCount(FlatType flatType) {
        if (flatType == FlatType.TWO_ROOM && numUnits1 > 0) {
            numUnits1--;
            return true;
        }
        if (flatType == FlatType.THREE_ROOM && numUnits2 > 0) {
            numUnits2--;
            return true;
        }
        return false; // Type invalid or no units left
    }

    public boolean increaseUnitCount(FlatType flatType) {
        if (flatType == FlatType.TWO_ROOM) {
            numUnits1++;
            return true;
        }
        if (flatType == FlatType.THREE_ROOM) {
            numUnits2++;
            return true;
        }
        return false; // Invalid type
    }

    public boolean addOfficer(String officerNric) {
        if (!InputUtil.validateNric(officerNric)) {
            throw new IllegalArgumentException("Invalid NRIC format for officer.");
        }
        if (!officerNrics.contains(officerNric)) {
            if (canAddOfficer()) {
                officerNrics.add(officerNric);
                return true;
            } else {
                throw new OperationError("No available officer slots.");
            }
        }
        return true; // Already present is considered success
    }

    public boolean removeOfficer(String officerNric) {
        return officerNrics.remove(officerNric);
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public void updateDetails(Project updates) {
        // Basic validation before applying changes
        String newName = updates.getProjectName();
        String newNeighborhood = updates.getNeighborhood();
        int n1 = updates.numUnits1;
        int p1 = updates.price1;
        int n2 = updates.numUnits2;
        int p2 = updates.price2;
        int slot = updates.officerSlot;
        Date newOd = updates.getOpeningDate();
        Date newCd = updates.getClosingDate();

        if (newName == null || newName.isEmpty()) throw new IllegalArgumentException("Project Name cannot be empty");
        if (newNeighborhood == null || newNeighborhood.isEmpty()) throw new IllegalArgumentException("Neighborhood cannot be empty");
        if (n1 < 0 || p1 < 0 || n2 < 0 || p2 < 0 || slot < 0) throw new IllegalArgumentException("Numeric values cannot be negative.");
        if (slot < 0 || slot > 10) throw new IllegalArgumentException("Officer slots must be between 0 and 10.");
        if (slot < officerNrics.size()) throw new IllegalArgumentException("Cannot reduce slots below current assigned officers.");
        if (newOd == null || newCd == null) throw new IllegalArgumentException("Dates must be valid date objects.");
        if (newCd.before(newOd)) throw new IllegalArgumentException("Closing date cannot be before opening date.");

        // Apply validated changes
        this.projectName = newName;
        this.neighborhood = newNeighborhood;
        this.numUnits1 = n1;
        this.price1 = p1;
        this.numUnits2 = n2;
        this.price2 = p2;
        this.officerSlot = slot;
        this.openingDate = newOd;
        this.closingDate = newCd;
    }

    public String toCsvString() {
        return projectName + "," + neighborhood + "," + type1.toString() + "," + numUnits1 + "," + price1 + "," 
                + type2.toString() + "," + numUnits2 + "," + price2 + "," + DateUtil.formatDate(openingDate) + "," 
                + DateUtil.formatDate(closingDate) + "," + managerNric + "," + officerSlot + "," + String.join(",", officerNrics) 
                + "," + visibility;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Project project = (Project) obj;
        return projectName.equals(project.projectName);
    }

    @Override
    public int hashCode() {
        return projectName.hashCode();
    }

    public String getDisplaySummary() {
        String visibilityStr = visibility ? "Visible" : "Hidden";
        String activeStatus = isCurrentlyVisibleAndActive() ? "Active" : "Inactive/Closed";
        return projectName + " (" + neighborhood + ") - Status: " + activeStatus + ", View: " + visibilityStr;
    }
    
    public String getDisplaySummary(String applicantName) {
        return "Project Name: " + projectName + " | Neighborhood: " + neighborhood + " | Applicant: " + applicantName;
    }
    public static String[] getHeaders() {
        return HEADERS;
    }
}

