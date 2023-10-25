package com.example.wish.entity;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;


@Data
@Embeddable
@Access(AccessType.FIELD)
public class Socials {

    @Column(length = 255)
    @URL
    private String facebook;

    @Column(length = 255)
    @URL
    private String vkontakte;

    @Column(length = 80)
    @URL
    private String instagram;

    @Column(length = 80)
    @URL
    private String telegram;

    @Column(length = 80)
    @URL
    private String viber;

    @Column(length = 80)
    @URL
    private String whatsApp;

    public Socials() {
    }
}
