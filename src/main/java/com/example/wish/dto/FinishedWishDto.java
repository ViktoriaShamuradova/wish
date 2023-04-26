package com.example.wish.dto;

import com.example.wish.entity.FinishWishStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinishedWishDto extends AbstractWishDto {
    private Date finish;
    private FinishWishStatus status;
    private Long executedProfileId;
    private Double earnKarma;

    private byte[] executedProfilePhoto;
    private String ownProfileFullName;
    private String ownProfileUid;
    private String executedProfileUid;
    private long wishId;
    private int watchCount;

}
