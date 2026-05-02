package com.offermate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatSessionCreateDTO {

    @NotNull(message = "岗位ID不能为空")
    private Long jobId;

    private Long targetUserId;

    private Long recruiterId;
}
