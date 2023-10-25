package com.example.wish.dto.wish;

import com.example.wish.constant.ExceptionMessage;
import com.example.wish.constant.Regex;
import com.example.wish.entity.Priority;
import com.example.wish.entity.Tag;
import com.example.wish.entity.TagName;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * This dto used for creating wish
 */

@Data
@NoArgsConstructor
public class CreateWishRequest {

    @Pattern(regexp = Regex.TITLE_DESCRIPTION, message = ExceptionMessage.TITLE_DESCRIPTION)
    @NotBlank(message = ExceptionMessage.NOT_BE_EMPTY)
    @Size(min = 1, max = 65, message = ExceptionMessage.A_LARGE_NUMBER_OF_CHARACTERS)
    private String title;

    @Pattern(regexp = Regex.TITLE_DESCRIPTION, message = ExceptionMessage.TITLE_DESCRIPTION)
    @Size(min = 1, max = 2000, message = ExceptionMessage.A_LARGE_NUMBER_OF_CHARACTERS)
    @NotBlank(message = ExceptionMessage.NOT_BE_EMPTY)
    private String description;

    @NotNull
    private Priority priority;

    private Set<TagName> tagNames;

}
