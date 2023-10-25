package com.example.wish.entity;

public enum Priority {
    ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN;

    public static Priority fromString(String value) {
        return switch (value.toLowerCase()) {
            case "one" -> ONE;
            case "two" -> TWO;
            case "three" -> THREE;
            case "four" -> FOUR;
            case "five" -> FIVE;
            case "six" -> SIX;
            case "seven" -> SEVEN;
            default -> throw new IllegalArgumentException("Invalid Priority value: " + value);
        };
    }
}