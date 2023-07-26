package com.example.wish.dto;

import com.example.wish.entity.CountryCode;
import com.example.wish.entity.Socials;
import com.example.wish.entity.Sex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileDetails {

    private String firstname;
    private String lastname;
    private String phone;
    private Date birthday;
    private CountryCode countryCode;
    private String city;
    private Sex sex;
    private Socials socials;
    private String photo;
}
