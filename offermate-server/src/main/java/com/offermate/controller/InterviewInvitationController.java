package com.offermate.controller;

import com.offermate.common.Result;
import com.offermate.annotation.OperationLog;
import com.offermate.dto.InterviewCreateDTO;
import com.offermate.service.InterviewInvitationService;
import com.offermate.vo.InterviewInvitationVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@Validated
public class InterviewInvitationController {

    private final InterviewInvitationService interviewInvitationService;

    @PostMapping
    @OperationLog(module = "面试邀请", operation = "发送面试邀请")
    public Result<Void> createInterview(@Valid @RequestBody InterviewCreateDTO dto) {
        interviewInvitationService.createInterview(dto);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<InterviewInvitationVO>> listMyInterviews() {
        return Result.success(interviewInvitationService.listMyInterviews());
    }

    @GetMapping("/company")
    public Result<List<InterviewInvitationVO>> listCompanyInterviews() {
        return Result.success(interviewInvitationService.listCompanyInterviews());
    }

    @PutMapping("/{id}/accept")
    @OperationLog(module = "面试邀请", operation = "接受面试邀请")
    public Result<Void> acceptInterview(@PathVariable @Min(value = 1, message = "面试邀请ID不合法") Long id) {
        interviewInvitationService.acceptInterview(id);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @OperationLog(module = "面试邀请", operation = "拒绝面试邀请")
    public Result<Void> rejectInterview(@PathVariable @Min(value = 1, message = "面试邀请ID不合法") Long id) {
        interviewInvitationService.rejectInterview(id);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @OperationLog(module = "面试邀请", operation = "取消面试邀请")
    public Result<Void> cancelInterview(@PathVariable @Min(value = 1, message = "面试邀请ID不合法") Long id) {
        interviewInvitationService.cancelInterview(id);
        return Result.success();
    }
}
