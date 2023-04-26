package com.example.wish.model.search_request;

import com.example.wish.entity.Priority;
import com.example.wish.entity.Sex;
import com.example.wish.entity.Tag;
import com.example.wish.entity.WishStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.Set;

@Data
@NoArgsConstructor
public class WishSearchRequest {
    private String title;
    private Set<Priority> priorities;
    private Sex profileSex;
    private String country;
    private Integer minAge;
    private Integer maxAge;
    private Set<Tag> tags;
    @JsonIgnore
    private WishStatus status;
    @JsonIgnore
    private Date fromDate;
    @JsonIgnore
    private Date toDate;
}
