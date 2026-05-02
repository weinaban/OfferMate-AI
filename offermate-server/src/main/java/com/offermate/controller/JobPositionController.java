package com.offermate.controller;

import com.offermate.common.Result;
import com.offermate.annotation.OperationLog;
import com.offermate.common.result.PageResult;
import com.offermate.dto.JobCreateDTO;
import com.offermate.dto.JobPageQueryDTO;
import com.offermate.dto.JobSearchDTO;
import com.offermate.dto.JobUpdateDTO;
import com.offermate.service.JobPositionService;
import com.offermate.vo.JobDetailVO;
import com.offermate.vo.JobPageVO;
import com.offermate.vo.SyncResultVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Validated
public class JobPositionController {

    private final JobPositionService jobPositionService;

    @PostMapping
    @OperationLog(module = "岗位管理", operation = "发布岗位")
    public Result<Void> createJob(@Valid @RequestBody JobCreateDTO dto) {
        jobPositionService.createJob(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @OperationLog(module = "岗位管理", operation = "修改岗位")
    public Result<Void> updateJob(@PathVariable @Min(value = 1, message = "岗位ID不合法") Long id,
                                  @Valid @RequestBody JobUpdateDTO dto) {
        jobPositionService.updateJob(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "岗位管理", operation = "删除岗位")
    public Result<Void> deleteJob(@PathVariable @Min(value = 1, message = "岗位ID不合法") Long id) {
        jobPositionService.deleteJob(id);
        return Result.success();
    }

    @PutMapping("/{id}/offline")
    @OperationLog(module = "岗位管理", operation = "下架岗位")
    public Result<Void> offlineJob(@PathVariable @Min(value = 1, message = "岗位ID不合法") Long id) {
        jobPositionService.offlineJob(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<JobDetailVO> getJobDetail(@PathVariable @Min(value = 1, message = "岗位ID不合法") Long id) {
        return Result.success(jobPositionService.getJobDetail(id));
    }

    @GetMapping("/page")
    public Result<PageResult<JobPageVO>> pageJobs(@Valid @ModelAttribute JobPageQueryDTO dto) {
        return Result.success(jobPositionService.pageJobs(dto));
    }

    @GetMapping("/search")
    public Result<PageResult<JobPageVO>> searchJobs(@Valid @ModelAttribute JobSearchDTO dto) {
        return Result.success(jobPositionService.searchJobs(dto));
    }

    @PostMapping("/es/sync")
    public Result<SyncResultVO> syncJobsToEs() {
        return Result.success(new SyncResultVO(jobPositionService.syncJobsToEs()));
    }

    @GetMapping("/company")
    public Result<List<JobPageVO>> listCompanyJobs() {
        return Result.success(jobPositionService.listCompanyJobs());
    }
}
