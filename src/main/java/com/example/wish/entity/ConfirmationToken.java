package com.example.wish.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Entity
@Table(name = "confirmation_token_test_v2", indexes = {
        @Index(name = "id_confirmation_token_token", columnList = "token")
})
public class ConfirmationToken {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    //почему false?
    @Column(nullable = false)
    private LocalDateTime confirmedAt;

    @ManyToOne()
    @JoinColumn(
            nullable = false,
            name = "profile_id")
    private Profile profile;

    public ConfirmationToken(String token,
                             LocalDateTime createdAt,
                             LocalDateTime expiresAt,
                             Profile profile) {
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.profile = profile;
    }

    public ConfirmationToken(String token) {
        this.token = token;
    }
}
