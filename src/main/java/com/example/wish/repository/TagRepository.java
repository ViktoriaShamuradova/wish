package com.example.wish.repository;

import com.example.wish.entity.Tag;
import com.example.wish.entity.TagName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findByTagName(TagName tagName);
}
