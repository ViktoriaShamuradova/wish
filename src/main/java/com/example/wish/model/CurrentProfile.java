package com.example.wish.model;

import com.example.wish.entity.Profile;
import com.example.wish.entity.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

/**
 * username is email. it is determined in constructor
 */
public class CurrentProfile extends User {
    private static final long serialVersionUID = 3850489832510630519L;
    private final long id;
    private final String email;

    public CurrentProfile(Profile profile) {
        super(profile.getEmail(), profile.getPassword(),
                profile.getEnabled(), true, true,
                !profile.getLocked(),
                Collections.singleton(new SimpleGrantedAuthority(Role.USER.name())));
        this.id = profile.getId();
        this.email = profile.getEmail();
    }

    public String getEmail() {
        return email;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("CurrentProfile [id=%s, username=%s]", id);
    }//getUsername()
}