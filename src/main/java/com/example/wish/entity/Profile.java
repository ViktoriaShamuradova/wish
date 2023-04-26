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
import java.util.Date;
import java.util.List;
import java.util.Objects;


@ToString
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
    private String firstname;

    @Column(name = "last_name", length = 20)
    private String lastname;

    @Column(name = "password", nullable = false, length = 100)
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

    @Column(length = 20)
    @Size(max = 20)
    private String phone;

    @Column
    @Size(max = 60)
    private String country;

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

    @Lob
    @Column(name = "photo")
    private byte[] photo;

    @Embedded
    private Contact contact;

    @Column(nullable = false)
    private Boolean active;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "ownProfile", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Wish> ownWishes;

    @ManyToMany(mappedBy = "profiles")
    private List<EnergyPractice> energyPractices;

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
}
