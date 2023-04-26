package com.example.wish.util;

import java.util.Random;

public class DataUtil {

    public static String normalizeName(String name) {
        return name.trim().toLowerCase();
    }

    public static String generateRandomString(String alphabet, int letterCount) {
        Random r = new Random();
        StringBuilder uid = new StringBuilder();
        for (int i = 0; i < letterCount; i++) {
            uid.append(alphabet.charAt(r.nextInt(alphabet.length())));
        }
        return uid.toString();
    }

    public static String capitalizeName(String name) {
        name = name.trim().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }


}