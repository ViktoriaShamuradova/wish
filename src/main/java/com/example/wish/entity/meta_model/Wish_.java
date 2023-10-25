package com.example.wish.entity.meta_model;

import com.example.wish.entity.*;


import javax.annotation.processing.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import java.util.Date;
import java.util.Set;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Wish.class)
public class Wish_ {
    public static volatile SingularAttribute<Wish, String> description;
    public static volatile SingularAttribute<Wish, String> title;
    public static volatile SingularAttribute<Wish, Long> id;
    public static volatile SingularAttribute<Wish, Priority> priority;
    public static volatile SingularAttribute<Wish, WishStatus> status;
    public static volatile SingularAttribute<Wish, Date> created;
    public static volatile SingularAttribute<Wish, Integer> watchCount;
    public static volatile SingularAttribute<Wish, Profile> ownProfile;
    public static volatile SingularAttribute<Wish, Set<TagName>> tags;


    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";
    public static final String TAGS = "tags";
    public static final String PRIORITY = "priority";
    public static final String CREATED = "created";
    public static final String STATUS = "status";
    public static final String WATCH_COUNT = "watchCount";
    public static final String OWN_PROFILE = "ownProfile";
    public static final String ID = "id";

}

