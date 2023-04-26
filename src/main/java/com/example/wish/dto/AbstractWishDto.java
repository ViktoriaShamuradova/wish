package com.example.wish.dto;

import com.example.wish.constant.ExceptionMessage;
import com.example.wish.constant.Regex;
import com.example.wish.entity.Priority;
import com.example.wish.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbstractWishDto {

    @Pattern(regexp = Regex.TITLE_DESCRIPTION, message = ExceptionMessage.TITLE_DESCRIPTION)
    @NotBlank(message = ExceptionMessage.NOT_BE_EMPTY)
    @Size(min = 2, max = 65, message = ExceptionMessage.A_LARGE_NUMBER_OF_CHARACTERS)
    private String title;

    @Pattern(regexp = Regex.TITLE_DESCRIPTION, message = ExceptionMessage.TITLE_DESCRIPTION)
    @Size(min = 2, max = 2000, message = ExceptionMessage.A_LARGE_NUMBER_OF_CHARACTERS)
    @NotBlank(message = ExceptionMessage.NOT_BE_EMPTY)
    private String description;

    private Priority priority;
    private Set<Tag> tags;

    private Date created;

      private byte[] photo;


    public AbstractWishDto(WishDto wish) {
        this.title = wish.getTitle();
        this.description = wish.getDescription();
        this.priority = wish.getPriority();
        this.tags = wish.getTags();
        this.photo = wish.getPhoto();
        this.created = wish.getCreated();
    }
}
