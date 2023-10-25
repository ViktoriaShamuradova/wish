package com.example.wish.service.impl;

import com.example.wish.entity.*;
import com.example.wish.exception.CantCompleteClientRequestException;
import com.example.wish.repository.ProfileRepository;
import com.example.wish.service.SocialService;
import com.example.wish.util.DataBuilder;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleService implements SocialService<GoogleIdToken> {

    private final ProfileRepository profileRepository;

    @Value("${generate.uid.alphabet}")
    private String generateUidAlphabet;
    @Value("${generate.uid.suffix.length}")
    private int generateUidSuffixLength;
    @Value("${generate.uid.max.try.count}")
    private int maxTryCountToGenerate;

    @Override
    public @NotNull Profile loginViaSocialNetwork(GoogleIdToken idToken) {
        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();
        String pictureUrl = (String) payload.get("picture");
        String familyName = (String) payload.get("family_name");
        String givenName = (String) payload.get("given_name");

        Optional<Profile> byEmail = profileRepository.findByEmail(email);

        if (byEmail.isPresent()) {
            return byEmail.get();
        } else {
            Profile profile = new Profile();
            profile.setActive(true);
            profile.setUid(generateProfileUid(email));
            profile.setEmail(email);
            profile.setFirstName(givenName);
            profile.setLastName(familyName);
            profile.setRole(Role.USER);
            profile.setStatus(ProfileStatus.RED);
            profile.setStatusLevel(ProfileStatusLevel.PERSON);
            profile.setCreated(new Timestamp(System.currentTimeMillis()));
            profile.setKarma(0);
            profile.setProvider(Provider.GOOGLE);
            profile.setLocked(false);
            profile.setEnabled(true);

            profileRepository.save(profile);

            return profile;
        }

    }

    private String generateProfileUid(String email) {
        String baseUid = DataBuilder.buildProfileUid(email);

        for (int i = 0; profileRepository.countByUid(baseUid) > 0; i++) {
            baseUid = DataBuilder.rebuildUidWithRandomSuffix(baseUid, generateUidAlphabet, generateUidSuffixLength);
            if (i >= maxTryCountToGenerate) {
                throw new CantCompleteClientRequestException("Can't generate unique uid for profile: " + baseUid + ": maxTryCountToGenerateUid detected");
            }
        }
        return baseUid;
    }

}
