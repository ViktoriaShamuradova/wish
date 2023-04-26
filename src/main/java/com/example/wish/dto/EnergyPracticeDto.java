package com.example.wish.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class EnergyPracticeDto {

    private Long id;

    private String title;

    private String description;

    private byte[] photo;

    private String link;

}
