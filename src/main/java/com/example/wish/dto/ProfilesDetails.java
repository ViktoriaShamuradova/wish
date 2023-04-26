package com.example.wish.dto;

import com.example.wish.entity.Contact;
import com.example.wish.entity.ProfileStatus;
import com.example.wish.entity.ProfileStatusLevel;
import com.example.wish.entity.Sex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfilesDetails {

    private String uid;
    private String firstName;
    private String lastName;
    private Sex sex;
    private double karma;
    private Integer age;
    private String phone;
    private String country;
    private String city;
    private ProfileStatus status;
    private ProfileStatusLevel statusLevel;
    private List<AbstractWishDto> ownWishes;
    private byte[] photo;
    private Contact contact;
}