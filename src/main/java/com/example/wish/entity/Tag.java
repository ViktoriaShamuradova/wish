package com.example.wish.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
//нужно вставить все теги в таблицу из enum
@Entity
@Table(name = "tag_test_v2")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_name", unique = true)
    private TagName tagName;

    @ManyToMany(mappedBy = "tags")
    private Set<Wish> wishes = new HashSet<>();

    // constructors, getters, setters...

    public TagName getTagName() {
        return tagName;
    }

    public void setTagName(TagName tagName) {
        this.tagName = tagName;
    }
}
