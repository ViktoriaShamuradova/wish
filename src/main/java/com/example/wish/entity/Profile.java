package com.example.wish.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;

//пересмотреь поле active. пользователь одинаково входит в систему при active true and false
//данное поле нигде пока не используется

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Entity
@Table(name = "profile_test_v2")
public class Profile implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String uid;

    @Column(name = "first_name", length = 15)
    private String firstName;

    @Column(name = "last_name", length = 20)
    private String lastName;

    @Column(name = "password", length = 100)
    @JsonIgnore
    private String password;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    @NotNull
    @Email(message = "not valid email")
    private String email;

    @Column
    private Boolean locked;

    @Column
    private Boolean enabled;

    @Column(name = "birth_date")
    @Temporal(TemporalType.DATE)
    private Date birthday;

    @Column(name = "sex")
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(length = 20)
    @Size(max = 20)
    private String phone;

    @Column(name = "country_code")
    @Enumerated(EnumType.STRING)
    private CountryCode countryCode;

    @Column()
    @Size(max = 100)
    private String city;

    @Column(name = "karma")
    @OrderBy("karma DESC")
    private double karma;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProfileStatus status;

    @Column(name = "status_level")
    @Enumerated(EnumType.STRING)
    private ProfileStatusLevel statusLevel;

    @Column()
    private Timestamp created;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private ProfileImage image;

    @Embedded
    private Socials socials;

    @Column(nullable = false)
    private Boolean active;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "profile")
    private List<Token> tokens;

    @OneToMany(mappedBy = "ownProfile",
            fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Wish> ownWishes;

    @ManyToMany(mappedBy = "profiles", cascade = CascadeType.REMOVE)
    private List<EnergyPractice> energyPractices;

    @ManyToMany(cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "profile_favorite",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "favorite_profile_id")
    )
    private Set<Profile> favoriteProfiles = new HashSet<>();


    public Set<Profile> getFavoriteProfiles() {
        return favoriteProfiles;
    }

    public void setFavoriteProfiles(Set<Profile> favoriteProfiles) {
        this.favoriteProfiles = favoriteProfiles;
    }


    public void addFavoriteProfile(Profile profile) {
        favoriteProfiles.add(profile);
        // profile.getFavoriteProfiles().add(this);
    }

    public void removeFavoriteProfile(Profile profile) {
        favoriteProfiles.remove(profile);
        //profile.getFavoriteProfiles().remove(this);
    }

    @Transient
    public Integer getAge() {
        if (!(birthday == null)) {
            LocalDate date;
            if (birthday instanceof java.sql.Date) {
                date = ((java.sql.Date) birthday).toLocalDate();
            } else {
                date = birthday.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            return Period.between(date, LocalDate.now()).getYears();
        } else {
            return null;
        }
    }

    public List<Wish> addInOwnWishes(Wish w) {
        getOwnWishes().add(w);
        w.setOwnProfile(this);

        return getOwnWishes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Profile profile = (Profile) o;
        return id != null && Objects.equals(id, profile.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", locked=" + locked +
                ", enabled=" + enabled +
                ", birthday=" + birthday +
                ", sex=" + sex +
                ", phone='" + phone + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", city='" + city + '\'' +
                ", karma=" + karma +
                ", status=" + status +
                ", statusLevel=" + statusLevel +
                ", created=" + created +
                ", socials=" + socials +
                ", active=" + active +
                ", role=" + role +
                '}';
    }
}
