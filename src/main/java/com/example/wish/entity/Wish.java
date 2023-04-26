package com.example.wish.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

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
@Table(name = "wish_test_v2")
public class Wish {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "description")
    @NotNull
    private String description;

    @Column(name = "title")
    @NotNull
    private String title;

    @ElementCollection(targetClass = Tag.class)
    @CollectionTable(name = "wish_tag", joinColumns = @JoinColumn(name = "wish_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tag_name")
    @OrderColumn
    private Set<Tag> tags;

    @Column(name = "priority")
    @NotNull
    @Enumerated(EnumType.STRING)
    private Priority priority;

    /**
     * only need for sort by priority
     */
    @Formula("(CASE WHEN priority = 'ONE' THEN 1 WHEN priority = 'TWO' THEN 2 WHEN priority = 'THREE' THEN 3 WHEN priority = 'FOUR' THEN 4 WHEN priority ='FIVE' THEN 5 WHEN priority ='SIX' THEN 6 WHEN priority ='SEVEN' THEN 7 END)")
    private int priorityRank;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private WishStatus status;

    @Lob
    @Column(name = "photo")
    private byte[] photo;

    @Column()
    private Date created;

    @Column(columnDefinition = "integer default 0", nullable = false)
    private int watchCount;

    @JoinColumn(name = "id_own_profile", nullable = false)
    @ManyToOne
    @ToString.Exclude
    private Profile ownProfile;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Wish wish = (Wish) o;
        return id != null && Objects.equals(id, wish.id);
    }


    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
