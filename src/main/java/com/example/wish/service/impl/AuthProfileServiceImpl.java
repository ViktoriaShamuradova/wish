package com.example.wish.service.impl;

import com.example.wish.entity.Profile;
import com.example.wish.model.CurrentProfile;
import com.example.wish.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthProfileServiceImpl implements UserDetailsService {
    private final static String USER_NOT_FOUND_MSG = "profile with email %s not found";
    private final ProfileRepository profileRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Profile> profile = findProfile(username);

        if (profile.isPresent()) {
            return new CurrentProfile(profile.get());
        } else {
            log.error("Profile {} not found", username);
            throw new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, username));
        }
    }


    private Optional<Profile> findProfile(String anyUniqId) {
        Optional<Profile> profile = profileRepository.findByEmail(anyUniqId);
        if (profile.isEmpty()) {
            profile = profileRepository.findByPhone(anyUniqId);
        }
        return profile;
    }

}
