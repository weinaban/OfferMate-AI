package com.offermate.controller;

import com.offermate.common.Result;
import com.offermate.annotation.OperationLog;
import com.offermate.dto.AiInterviewQuestionsDTO;
import com.offermate.dto.AiJobDescriptionDTO;
import com.offermate.dto.AiResumeOptimizeFormDTO;
import com.offermate.dto.AiResumeOptimizeSectionDTO;
import com.offermate.dto.AiResumeOptimizeDTO;
import com.offermate.service.AiService;
import com.offermate.vo.AiResumeOptimizeFormVO;
import com.offermate.vo.AiResumeSectionOptimizeVO;
import com.offermate.vo.AiResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/resume/optimize")
    @OperationLog(module = "AI模块", operation = "AI简历优化")
    public Result<AiResultVO> optimizeResume(@Valid @RequestBody AiResumeOptimizeDTO dto) {
        return Result.success(aiService.optimizeResume(dto));
    }

    @PostMapping("/resume/optimize-form")
    public Result<AiResumeOptimizeFormVO> optimizeResumeForm(@RequestBody AiResumeOptimizeFormDTO dto) {
        return Result.success(aiService.optimizeResumeForm(dto));
    }

    @PostMapping("/resume/optimize-section")
    public Result<AiResumeSectionOptimizeVO> optimizeResumeSection(@RequestBody AiResumeOptimizeSectionDTO dto) {
        return Result.success(aiService.optimizeResumeSection(dto));
    }

    @PostMapping("/interview/questions")
    @OperationLog(module = "AI模块", operation = "AI生成面试题")
    public Result<AiResultVO> generateInterviewQuestions(@Valid @RequestBody AiInterviewQuestionsDTO dto) {
        return Result.success(aiService.generateInterviewQuestions(dto));
    }

    @PostMapping("/job/description")
    @OperationLog(module = "AI模块", operation = "AI生成岗位描述")
    public Result<AiResultVO> generateJobDescription(@Valid @RequestBody AiJobDescriptionDTO dto) {
        return Result.success(aiService.generateJobDescription(dto));
    }
}
