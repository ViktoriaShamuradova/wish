package com.example.wish.entity.meta_model;

import com.example.wish.entity.*;

import javax.annotation.processing.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Profile.class)
public class Profile_ {

    public static volatile SingularAttribute<Profile, String> uid;
    public static volatile SingularAttribute<Profile, String> firstname;
    public static volatile SingularAttribute<Profile, String> lastname;
    public static volatile SingularAttribute<Profile, String> email;
    public static volatile SingularAttribute<Profile, Date> birthday;
    public static volatile SingularAttribute<Profile, Sex> sex;
    public static volatile SingularAttribute<Profile, String> phone;
    public static volatile SingularAttribute<Profile, String> country;
    public static volatile SingularAttribute<Profile, String> city;
    public static volatile SingularAttribute<Profile, Double> karma;
    public static volatile SingularAttribute<Profile, ProfileStatus> status;
    public static volatile SingularAttribute<Profile, ProfileStatusLevel> statusLevel;
    public static volatile SingularAttribute<Profile, Timestamp> created;
    public static volatile SingularAttribute<Profile, Contact> contact;
    public static volatile SingularAttribute<Profile, Long> id;
    public static volatile SingularAttribute<Profile, Boolean> active;
    public static volatile SingularAttribute<Profile, Role> role;
    public static volatile SingularAttribute<Profile, List<Wish>> ownWishes;


    public static final String UID = "uid";
    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    public static final String EMAIL = "email";
    public static final String BIRTHDAY = "birthday";
    public static final String SEX = "sex";
    public static final String PHONE = "phone";
    public static final String COUNTRY = "country";
    public static final String ID = "id";
    public static final String CITY = "city";
    public static final String KARMA = "karma";
    public static final String STATUS = "status";
    public static final String STATUS_LEVEL = "statusLevel";
    public static final String CREATED = "created";
    public static final String CONTACT = "contact";
    public static final String OWN_WISHES = "ownWishes";

}
