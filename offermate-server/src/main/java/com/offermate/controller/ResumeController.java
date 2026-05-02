package com.offermate.controller;

import com.offermate.common.Result;
import com.offermate.dto.ResumeCreateDTO;
import com.offermate.dto.ResumeUpdateDTO;
import com.offermate.service.ResumeService;
import com.offermate.vo.ResumeVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Validated
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public Result<Void> createResume(@Valid @RequestBody ResumeCreateDTO dto) {
        resumeService.createResume(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateResume(@PathVariable @Min(value = 1, message = "简历ID不合法") Long id,
                                     @Valid @RequestBody ResumeUpdateDTO dto) {
        resumeService.updateResume(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteResume(@PathVariable @Min(value = 1, message = "简历ID不合法") Long id) {
        resumeService.deleteResume(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<ResumeVO> getResumeDetail(@PathVariable @Min(value = 1, message = "简历ID不合法") Long id) {
        return Result.success(resumeService.getResumeDetail(id));
    }

    @GetMapping("/my")
    public Result<List<ResumeVO>> listMyResumes() {
        return Result.success(resumeService.listMyResumes());
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefaultResume(@PathVariable @Min(value = 1, message = "简历ID不合法") Long id) {
        resumeService.setDefaultResume(id);
        return Result.success();
    }
}
