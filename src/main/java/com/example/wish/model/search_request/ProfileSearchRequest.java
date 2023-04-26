package com.example.wish.model.search_request;

import com.example.wish.entity.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
public class ProfileSearchRequest {

    private String firstnameLastname;
    private ProfileStatus profileStatus;
    private ProfileStatusLevel statusLevel;
    private String country;
    private Sex profileSex;
    private Integer minAge;
    private Integer maxAge;


    @JsonIgnore
    private Date fromDate;
    @JsonIgnore
    private Date toDate;
    @JsonIgnore
    private String firstname;
    @JsonIgnore
    private String lastname;

}
