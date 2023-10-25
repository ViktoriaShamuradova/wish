package com.example.wish.entity;

public enum Sex {
    M, F;

    public static Sex fromString(String value) {
        return switch (value.toLowerCase()) {
            case "f" -> F;
            case "m" -> M;
            default -> throw new IllegalArgumentException("Invalid Priority value: " + value);
        };
    }
}
