package com.example.wish.dto.wish;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmWishRequest {

    @NotNull
    private long wishId;

    @NotNull
    private Boolean isConfirm;

    private String reasonOfFailedFromOwner;
}
