package com.example.wish.dto.wish;

import com.example.wish.entity.FinishWishStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinishedWishDto extends AbstractWishDto {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finish;
    private FinishWishStatus status;
    private Long executedProfileId;
    private Double earnKarma;
    private boolean anonymously;


    private long wishId;
    private int watchCount;
}
