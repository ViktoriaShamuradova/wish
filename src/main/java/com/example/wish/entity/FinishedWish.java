package com.example.wish.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Entity
@Table(name = "finished_wish_test_v2")
public class FinishedWish {
    @Id
    @GeneratedValue
    private Long id;

    @Column()
    private Date finish;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private FinishWishStatus status;

    @JoinColumn(name = "executed_profile_id", nullable = false)
    @ManyToOne
    @ToString.Exclude
    private Profile executedProfile;

    @Column
    private Double earnKarma;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "wish_id", nullable = false)
    private Wish wish;

    @Column
    private int attempts;

    @Column(columnDefinition = "boolean default false")
    private boolean anonymously;

    @Column
    private String reasonOfFailedFromOwner;

    @Column
    private String reasonOfFailedFromExecutor;

    public FinishedWish(Profile executingProfile, Wish wish, Timestamp from, FinishWishStatus status, int attempts) {
        this.executedProfile = executingProfile;
        this.wish = wish;
        this.finish = from;
        this.status = status;
        this.attempts = attempts;
    }

    public FinishedWish(Profile executingProfile, Wish wish, Timestamp from, FinishWishStatus status, String reasonOfFailedFromOwner, int attempts) {
        this.executedProfile = executingProfile;
        this.wish = wish;
        this.finish = from;
        this.status = status;
        this.reasonOfFailedFromOwner = reasonOfFailedFromOwner;
        this.attempts = attempts;
    }
}
