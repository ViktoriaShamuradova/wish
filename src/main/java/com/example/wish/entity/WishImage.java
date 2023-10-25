package com.example.wish.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "wish_image_test_v2")
public class WishImage {
    @Id
    @GeneratedValue
    private Long id;

    @Column()
    @NotNull
    private Date created;

    @Lob
    @Column(name = "image_data", nullable = false)
    private byte[] imageData;

}
