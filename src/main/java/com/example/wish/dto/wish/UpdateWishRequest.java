package com.example.wish.dto.wish;

import com.example.wish.constant.ExceptionMessage;
import com.example.wish.entity.Priority;
import com.example.wish.entity.TagName;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@NoArgsConstructor
public class UpdateWishRequest {

    @NotBlank(message = ExceptionMessage.NOT_BE_EMPTY)
    @NotNull
    private String title;

    @NotBlank(message = ExceptionMessage.NOT_BE_EMPTY)
    @NotNull
    private String description;

    @NotNull
    private Priority priority;

    @NotNull
    private Set<TagName> tagNames;

}
