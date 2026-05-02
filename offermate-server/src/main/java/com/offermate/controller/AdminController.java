package com.offermate.controller;

import com.offermate.common.Result;
import com.offermate.common.result.PageResult;
import com.offermate.dto.AdminCompanyAuditPageQueryDTO;
import com.offermate.dto.AdminJobAuditPageQueryDTO;
import com.offermate.dto.AdminUserPageQueryDTO;
import com.offermate.dto.AuditDTO;
import com.offermate.dto.OperationLogQueryDTO;
import com.offermate.annotation.OperationLog;
import com.offermate.service.AdminService;
import com.offermate.service.OperationLogService;
import com.offermate.vo.AdminCompanyAuditVO;
import com.offermate.vo.AdminJobAuditVO;
import com.offermate.vo.AdminUserVO;
import com.offermate.vo.OperationLogVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final AdminService adminService;
    private final OperationLogService operationLogService;

    @GetMapping("/users")
    public Result<PageResult<AdminUserVO>> pageUsers(@Valid @ModelAttribute AdminUserPageQueryDTO dto) {
        return Result.success(adminService.pageUsers(dto));
    }

    @PutMapping("/users/{id}/disable")
    public Result<Void> disableUser(@PathVariable @Min(value = 1, message = "用户ID不合法") Long id) {
        adminService.disableUser(id);
        return Result.success();
    }

    @PutMapping("/users/{id}/enable")
    public Result<Void> enableUser(@PathVariable @Min(value = 1, message = "用户ID不合法") Long id) {
        adminService.enableUser(id);
        return Result.success();
    }

    @GetMapping("/companies/audit")
    public Result<PageResult<AdminCompanyAuditVO>> pageCompanyAudits(@Valid @ModelAttribute AdminCompanyAuditPageQueryDTO dto) {
        return Result.success(adminService.pageCompanyAudits(dto));
    }

    @PutMapping("/companies/{id}/audit")
    @OperationLog(module = "管理后台", operation = "审核企业")
    public Result<Void> auditCompany(@PathVariable @Min(value = 1, message = "企业ID不合法") Long id,
                                     @Valid @RequestBody AuditDTO dto) {
        adminService.auditCompany(id, dto);
        return Result.success();
    }

    @GetMapping("/jobs/audit")
    public Result<PageResult<AdminJobAuditVO>> pageJobAudits(@Valid @ModelAttribute AdminJobAuditPageQueryDTO dto) {
        return Result.success(adminService.pageJobAudits(dto));
    }

    @PutMapping("/jobs/{id}/audit")
    @OperationLog(module = "管理后台", operation = "审核岗位")
    public Result<Void> auditJob(@PathVariable @Min(value = 1, message = "岗位ID不合法") Long id,
                                 @Valid @RequestBody AuditDTO dto) {
        adminService.auditJob(id, dto);
        return Result.success();
    }

    @GetMapping("/operation-logs")
    public Result<PageResult<OperationLogVO>> pageOperationLogs(@Valid @ModelAttribute OperationLogQueryDTO dto) {
        return Result.success(operationLogService.pageLogs(dto));
    }
}
