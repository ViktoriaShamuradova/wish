package com.example.wish.dto;

import com.example.wish.entity.Socials;
import com.example.wish.entity.ProfileStatus;
import com.example.wish.entity.ProfileStatusLevel;
import com.example.wish.entity.Sex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileDto {

        private String uid;
        private String firstName;
        private String lastName;
        private String email;
        private Sex sex;
        private double karma;
        private Integer age;
        private Date birthday;
        private String phone;
        private String country;
        private String city;
        private ProfileStatus status;
        private ProfileStatusLevel statusLevel;
        private String photo;
        private Socials socials;

}
