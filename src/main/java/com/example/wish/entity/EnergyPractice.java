package com.example.wish.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
@Table(name = "energy_practice_test_v2")
public class EnergyPractice {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String title;

    @Column
    private String description;

    @Lob
    @Column(name = "photo")
    private byte[] photo;

    @Column
    private String link;

    @ManyToMany
    @JoinTable(
            name = "energy_practice_profile",
            joinColumns = @JoinColumn(name = "energy_practice_id"),
            inverseJoinColumns = @JoinColumn(name = "profile_id")
    )
    private List<Profile> profiles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnergyPractice)) return false;
        EnergyPractice that = (EnergyPractice) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getDescription(), that.getDescription()) && Arrays.equals(getPhoto(), that.getPhoto()) && Objects.equals(getLink(), that.getLink());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getId(), getTitle(), getDescription(), getLink());
        result = 31 * result + Arrays.hashCode(getPhoto());
        return result;
    }
}
