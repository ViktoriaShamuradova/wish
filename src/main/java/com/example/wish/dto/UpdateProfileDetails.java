package com.example.wish.dto;

import com.example.wish.entity.Contact;
import com.example.wish.entity.Sex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileDetails {

    private String name;
    private String phone;
    private Date birthday;
    private String country;
    private String city;
    private Sex sex;
    private Contact contact;
}
