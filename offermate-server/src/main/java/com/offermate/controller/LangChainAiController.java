package com.offermate.controller;

import com.offermate.common.Result;
import com.offermate.annotation.OperationLog;
import com.offermate.dto.AiInterviewAnswerDTO;
import com.offermate.dto.AiInterviewSessionCreateDTO;
import com.offermate.dto.AiJobMatchDTO;
import com.offermate.service.LangChainAiService;
import com.offermate.vo.AiInterviewAnswerResultVO;
import com.offermate.vo.AiInterviewQuestionVO;
import com.offermate.vo.AiInterviewReportVO;
import com.offermate.vo.AiInterviewSessionVO;
import com.offermate.vo.AiJobMatchVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Validated
public class LangChainAiController {

    private final LangChainAiService langChainAiService;

    @PostMapping("/job/match")
    @OperationLog(module = "AI模块", operation = "AI岗位匹配")
    public Result<AiJobMatchVO> matchJob(@Valid @RequestBody AiJobMatchDTO dto) {
        return Result.success(langChainAiService.matchJob(dto));
    }

    @PostMapping("/interview/session")
    @OperationLog(module = "AI模拟面试", operation = "创建模拟面试会话")
    public Result<AiInterviewSessionVO> createInterviewSession(@Valid @RequestBody AiInterviewSessionCreateDTO dto) {
        return Result.success(langChainAiService.createInterviewSession(dto));
    }

    @PostMapping("/interview/session/{id}/question")
    @OperationLog(module = "AI模拟面试", operation = "生成面试问题")
    public Result<AiInterviewQuestionVO> generateQuestion(@PathVariable @Min(value = 1, message = "面试会话ID不合法") Long id) {
        return Result.success(langChainAiService.generateQuestion(id));
    }

    @PostMapping("/interview/session/{id}/answer")
    @OperationLog(module = "AI模拟面试", operation = "提交面试回答")
    public Result<AiInterviewAnswerResultVO> submitAnswer(@PathVariable @Min(value = 1, message = "面试会话ID不合法") Long id,
                                                          @Valid @RequestBody AiInterviewAnswerDTO dto) {
        return Result.success(langChainAiService.submitAnswer(id, dto));
    }

    @GetMapping("/interview/session/{id}/report")
    @OperationLog(module = "AI模拟面试", operation = "生成面试报告")
    public Result<AiInterviewReportVO> generateReport(@PathVariable @Min(value = 1, message = "面试会话ID不合法") Long id) {
        return Result.success(langChainAiService.generateReport(id));
    }
}
