package com.example.wish.dto;

import com.example.wish.entity.WishStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishDto extends AbstractWishDto {

    private static final SimpleDateFormat dateFormat
            = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private WishStatus wishStatus;

    private long id;
    private int watchCount;


    public WishDto(WishDto wish) {
        super(wish);
        this.wishStatus = wish.getWishStatus();
        this.id = wish.getId();
    }

}