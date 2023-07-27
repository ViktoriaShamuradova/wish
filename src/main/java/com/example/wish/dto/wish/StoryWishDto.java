package com.example.wish.dto.wish;

import com.example.wish.dto.AbstractWishDto;
import com.example.wish.dto.wish.FinishedWishDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * this class is used for profile wish history
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoryWishDto {
    private List<FinishedWishDto> anotherFinishedWishes;
    private List<FinishedWishDto> anotherNotFinishedWishes;
    private List<AbstractWishDto> ownWishes; //потому что здесь могут отображаться как завершенны, так и удаленные

}
