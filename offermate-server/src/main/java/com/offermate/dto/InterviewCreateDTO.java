package com.offermate.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.offermate.util.FlexibleLocalDateTimeDeserializer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewCreateDTO {

    @NotNull(message = "投递记录ID不能为空")
    private Long deliveryId;

    @NotNull(message = "面试时间不能为空")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime interviewTime;

    @Size(max = 255, message = "面试地址长度不能超过255")
    private String address;

    @Size(max = 50, message = "联系人长度不能超过50")
    private String contactName;

    @Size(max = 20, message = "联系电话长度不能超过20")
    private String contactPhone;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
