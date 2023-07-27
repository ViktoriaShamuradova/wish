package com.example.wish.dto.wish;

import com.example.wish.dto.AbstractWishDto;
import com.example.wish.entity.ExecuteStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecutingWishDto extends AbstractWishDto {

    private Long executingProfileId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finish; //когда закончится срок выполнения желания
    private ExecuteStatus executingStatus;
    private long wishId; //id of wish not executing wish id
    private String executingProfilePhoto;
    private String executingProfileUid;
    private int watchCount;
}
