package com.example.wish.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import javax.persistence.*;
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
@Table(name = "executing_wish_test_v2", indexes = {
        @Index(name = "idx_executing_wish_finish", columnList = "finish")
})
public class ExecutingWish {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExecuteStatus executingStatus;

    @Column()
    private Date finish;

    @JoinColumn(name = "executing_profile_id", nullable = false)
    @ManyToOne
    @ToString.Exclude
    private Profile executingProfile;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "wish_id", nullable = false)
    private Wish wish;

    public ExecutingWish(Wish wish) {
        this.wish = wish;
    }

}
