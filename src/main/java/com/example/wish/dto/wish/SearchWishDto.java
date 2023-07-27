package com.example.wish.dto.wish;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchWishDto extends WishDto {
    private String profileUid;
    private String profileFirstname;
    private String profileLastname;

    public SearchWishDto(WishDto wish) {
        super(wish);

    }
}
