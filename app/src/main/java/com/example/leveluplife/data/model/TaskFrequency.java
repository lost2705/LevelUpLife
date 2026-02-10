package com.example.leveluplife.data.model;

public enum TaskFrequency {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    ONE_TIME("One Time");

    private final String displayName;

    TaskFrequency(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TaskFrequency fromString(String text) {
        if (text == null) return ONE_TIME;
        switch (text.toUpperCase()) {
            case "DAILY": return DAILY;
            case "WEEKLY": return WEEKLY;
            default: return ONE_TIME;
        }
    }
}
