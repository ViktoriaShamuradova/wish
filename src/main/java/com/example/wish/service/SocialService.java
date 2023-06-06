package com.example.wish.service;

import com.example.wish.entity.Profile;

public interface SocialService<T> {

    Profile loginViaSocialNetwork(T t);


}
