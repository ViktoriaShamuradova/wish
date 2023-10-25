package com.example.wish.dto;

import com.example.wish.dto.wish.SearchWishDto;
import com.example.wish.entity.TagName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * Используется для отображения экрана поиска желаний.
 * В экране отображаются все категории (теги),
 * количество всех жеданий,
 * сами желания
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchScreenWishDto {
    List<SearchWishDto> wishes; //searchWishDto
    int countAllOfWishes;
    Set<TagName> tags;
}
