package com.example.wish.constant;

public final class Regex {
    public static final String TITLE_DESCRIPTION = "[\\p{L}\\p{N}\\p{P}\\s]+" ;//наличие букв, цифр, знаков препинания, пробела в любом порядке
    public static final String TITLE_DESCRIPTION2 = "^[\\p{L}\\p{N}\\s]+$";
}
