package com.example.wish.dto.profile;

import com.example.wish.entity.*;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;
    private String phone;
    private CountryCode countryCode;
    private String countryName;

    private String city;
    private ProfileStatus status;
    private ProfileStatusLevel statusLevel;
    private Socials socials;

    public ProfileDto(Profile profile) {
        this.uid = profile.getUid();
        this.firstName = profile.getFirstName();
        this.lastName = profile.getLastName();
        this.email = profile.getEmail();
        this.sex = profile.getSex();
        this.karma = profile.getKarma();
        this.age = profile.getAge();
        this.birthday = profile.getBirthday();
        this.phone = profile.getPhone();
        this.countryCode = profile.getCountryCode();
        this.city = profile.getCity();
        this.status = profile.getStatus();
        this.statusLevel = profile.getStatusLevel();
        this.socials = profile.getSocials();
    }

}
