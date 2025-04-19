package common;

public enum FlatType {
    TWO_ROOM(2),
    THREE_ROOM(3);

    private final int value;

    FlatType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    // Helper method to convert a string value to FlatType
    public static FlatType fromValue(int value) {
        for (FlatType flatType : FlatType.values()) {
            if (flatType.getValue() == value) {
                return flatType;
            }
        }
        throw new IllegalArgumentException("Invalid value for FlatType: " + value);
    }

    // Helper method to convert FlatType to a descriptive string
    public String toString() {
        return this.value + "-Room";
    }
}
