package com.example.wish.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Table(
        name = "profile_visitor_test_v2",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"profile_id", "wish_id"})
        },
        indexes = {
                @Index(name = "idx_wish_id", columnList = "wish_id")
        }
)
@Entity
public class ProfileVisitorWish implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    @ToString.Exclude
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "wish_id", nullable = false)
    private Wish wish;

    public ProfileVisitorWish(Profile profile, Wish wish) {
        this.profile = profile;
        this.wish = wish;
    }
}
