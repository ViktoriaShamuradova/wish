package com.example.wish.dto.profile;

import com.example.wish.entity.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfilesDetails {

    private String uid;
    private String firstName;
    private String lastName;
    private String email;
    private Sex sex;
    private double karma;
    private Integer age;
    private String phone;
    private CountryCode country;
    private String city;
    private ProfileStatus status;
    private ProfileStatusLevel statusLevel;
    private String photo;
    private Socials socials;
}