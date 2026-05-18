package com.offermate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offermate.ai.InterviewAssistant;
import com.offermate.common.Result;
import com.offermate.dto.AiInterviewAnswerDTO;
import com.offermate.dto.AiInterviewSessionCreateDTO;
import com.offermate.dto.AiJobMatchDTO;
import com.offermate.dto.LoginUserDTO;
import com.offermate.entity.AiCallLog;
import com.offermate.entity.AiInterviewMessage;
import com.offermate.entity.AiInterviewSession;
import com.offermate.entity.Company;
import com.offermate.entity.JobPosition;
import com.offermate.entity.Resume;
import com.offermate.exception.BusinessException;
import com.offermate.mapper.AiCallLogMapper;
import com.offermate.mapper.AiChatMemoryMapper;
import com.offermate.mapper.AiInterviewMessageMapper;
import com.offermate.mapper.AiInterviewSessionMapper;
import com.offermate.mq.RabbitMQProducer;
import com.offermate.mq.dto.AiLogMessage;
import com.offermate.service.AiLimitService;
import com.offermate.service.CompanyService;
import com.offermate.service.JobPositionService;
import com.offermate.service.LangChainAiService;
import com.offermate.service.ResumeService;
import com.offermate.util.AiSecurityUtils;
import com.offermate.util.UserContext;
import com.offermate.vo.AiInterviewAnswerResultVO;
import com.offermate.vo.AiInterviewQuestionVO;
import com.offermate.vo.AiInterviewReportVO;
import com.offermate.vo.AiInterviewSessionVO;
import com.offermate.vo.AiJobMatchVO;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 模拟面试核心业务。
 * <p>
 * 模型 / Memory / RAG 全部由 langchain4j-spring-boot-starter 自动装配：
 * <ul>
 *   <li>{@link ChatModel}：DashScope qwen-plus，无状态调用（岗位匹配、面试报告）</li>
 *   <li>{@link InterviewAssistant}：声明式 @AiService，串联 Memory + RAG，用于多轮面试问答</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LangChainAiServiceImpl implements LangChainAiService {

    private static final int SEEKER_ROLE = 1;
    private static final int SESSION_RUNNING = 1;
    private static final int SESSION_FINISHED = 2;
    private static final String AI_ERROR_MESSAGE = "AI 服务暂时不可用，请稍后再试";
    private static final String ROLE_SYSTEM = "system";
    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";
    private static final String JOB_MATCH = "JOB_MATCH";
    private static final String INTERVIEW_QUESTION = "INTERVIEW_QUESTION";
    private static final String INTERVIEW_ANSWER_SCORE = "INTERVIEW_ANSWER_SCORE";
    private static final String INTERVIEW_REPORT = "INTERVIEW_REPORT";

    private final ChatModel chatModel;
    private final InterviewAssistant interviewAssistant;
    private final ObjectMapper objectMapper;
    private final ResumeService resumeService;
    private final JobPositionService jobPositionService;
    private final CompanyService companyService;
    private final AiLimitService aiLimitService;
    private final AiInterviewSessionMapper aiInterviewSessionMapper;
    private final AiInterviewMessageMapper aiInterviewMessageMapper;
    private final AiChatMemoryMapper aiChatMemoryMapper;
    private final AiCallLogMapper aiCallLogMapper;
    private final RabbitMQProducer rabbitMQProducer;

    @Override
    public AiJobMatchVO matchJob(AiJobMatchDTO dto) {
        LoginUserDTO loginUser = checkSeeker();
        if (dto == null || dto.getResumeId() == null) {
            throw new BusinessException("简历ID不能为空");
        }
        if (dto.getJobId() == null) {
            throw new BusinessException("岗位ID不能为空");
        }

        Resume resume = getOwnedResume(dto.getResumeId(), loginUser.getUserId());
        JobPosition job = getJob(dto.getJobId());
        Company company = job.getCompanyId() == null ? null : companyService.getById(job.getCompanyId());

        String prompt = buildJobMatchPrompt(resume, job, company);
        AiSecurityUtils.checkPromptLength(prompt);
        aiLimitService.checkAndIncrement(loginUser.getUserId());
        String result = callChatModelAndLog(loginUser.getUserId(), JOB_MATCH, prompt);
        return parseJobMatchResult(result);
    }

    @Override
    public AiInterviewSessionVO createInterviewSession(AiInterviewSessionCreateDTO dto) {
        LoginUserDTO loginUser = checkSeeker();
        if (dto == null || dto.getJobId() == null) {
            throw new BusinessException("岗位ID不能为空");
        }

        JobPosition job = getJob(dto.getJobId());
        Resume resume = null;
        if (dto.getResumeId() != null) {
            resume = getOwnedResume(dto.getResumeId(), loginUser.getUserId());
        }

        AiInterviewSession session = new AiInterviewSession();
        session.setUserId(loginUser.getUserId());
        session.setJobId(job.getId());
        session.setResumeId(resume == null ? null : resume.getId());
        session.setTitle(nullToEmpty(job.getTitle()) + "模拟面试");
        session.setStatus(SESSION_RUNNING);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        aiInterviewSessionMapper.insert(session);

        AiInterviewMessage systemMessage = new AiInterviewMessage();
        systemMessage.setSessionId(session.getId());
        systemMessage.setRole(ROLE_SYSTEM);
        systemMessage.setContent(buildInterviewSystemPrompt(job, resume));
        systemMessage.setCreateTime(LocalDateTime.now());
        aiInterviewMessageMapper.insert(systemMessage);

        return toSessionVO(session);
    }

    @Override
    public AiInterviewQuestionVO generateQuestion(Long sessionId) {
        LoginUserDTO loginUser = checkSeeker();
        AiInterviewSession session = getOwnedRunningSession(sessionId, loginUser.getUserId());
        JobPosition job = getJob(session.getJobId());
        Resume resume = session.getResumeId() == null ? null : resumeService.getById(session.getResumeId());

        int round = countUserAnswers(sessionId) + 1;
        String userMessage = buildQuestionUserMessage(job, resume, round);
        AiSecurityUtils.checkPromptLength(userMessage);
        aiLimitService.checkAndIncrement(loginUser.getUserId());

        String question;
        try {
            question = cleanPlainText(interviewAssistant.askNextQuestion(sessionId, userMessage));
        } catch (Exception e) {
            log.error("LangChain4j 面试出题失败 sessionId={}", sessionId, e);
            saveLog(loginUser.getUserId(), INTERVIEW_QUESTION, userMessage, AI_ERROR_MESSAGE);
            throw new BusinessException(AI_ERROR_MESSAGE);
        }
        if (!StringUtils.hasText(question)) {
            throw new BusinessException(AI_ERROR_MESSAGE);
        }
        saveLog(loginUser.getUserId(), INTERVIEW_QUESTION, userMessage, question);

        AiInterviewMessage message = new AiInterviewMessage();
        message.setSessionId(sessionId);
        message.setRole(ROLE_ASSISTANT);
        message.setContent(question);
        message.setCreateTime(LocalDateTime.now());
        aiInterviewMessageMapper.insert(message);

        AiInterviewQuestionVO vo = new AiInterviewQuestionVO();
        vo.setSessionId(sessionId);
        vo.setQuestion(question);
        vo.setRound(round);
        return vo;
    }

    @Override
    public AiInterviewAnswerResultVO submitAnswer(Long sessionId, AiInterviewAnswerDTO dto) {
        LoginUserDTO loginUser = checkSeeker();
        AiInterviewSession session = getOwnedRunningSession(sessionId, loginUser.getUserId());
        if (dto == null || !StringUtils.hasText(dto.getAnswer())) {
            throw new BusinessException("回答内容不能为空");
        }

        String answer = dto.getAnswer().trim();
        AiInterviewMessage userMessage = new AiInterviewMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setRole(ROLE_USER);
        userMessage.setContent(answer);
        userMessage.setCreateTime(LocalDateTime.now());
        aiInterviewMessageMapper.insert(userMessage);

        JobPosition job = getJob(session.getJobId());
        Resume resume = session.getResumeId() == null ? null : resumeService.getById(session.getResumeId());
        int answerCount = countUserAnswers(sessionId);
        String evaluatePrompt = buildAnswerUserMessage(job, resume, answer, answerCount);
        AiSecurityUtils.checkPromptLength(evaluatePrompt);
        aiLimitService.checkAndIncrement(loginUser.getUserId());

        String result;
        try {
            result = interviewAssistant.evaluateAnswer(sessionId, evaluatePrompt);
        } catch (Exception e) {
            log.error("LangChain4j 面试评分失败 sessionId={}", sessionId, e);
            saveLog(loginUser.getUserId(), INTERVIEW_ANSWER_SCORE, evaluatePrompt, AI_ERROR_MESSAGE);
            throw new BusinessException(AI_ERROR_MESSAGE);
        }
        if (!StringUtils.hasText(result)) {
            throw new BusinessException(AI_ERROR_MESSAGE);
        }
        saveLog(loginUser.getUserId(), INTERVIEW_ANSWER_SCORE, evaluatePrompt, result);

        AiInterviewAnswerResultVO vo = parseAnswerResult(result);
        vo.setSessionId(sessionId);

        AiInterviewMessage assistantMessage = new AiInterviewMessage();
        assistantMessage.setSessionId(sessionId);
        assistantMessage.setRole(ROLE_ASSISTANT);
        assistantMessage.setContent(result);
        assistantMessage.setScore(vo.getScore());
        assistantMessage.setSuggestion(vo.getSuggestion());
        assistantMessage.setCreateTime(LocalDateTime.now());
        aiInterviewMessageMapper.insert(assistantMessage);

        if (Boolean.TRUE.equals(vo.getFinished())) {
            updateSessionStatus(sessionId, SESSION_FINISHED);
        }
        return vo;
    }

    @Override
    public AiInterviewReportVO generateReport(Long sessionId) {
        LoginUserDTO loginUser = checkSeeker();
        AiInterviewSession session = getOwnedSession(sessionId, loginUser.getUserId());
        List<AiInterviewMessage> messages = listAllMessages(sessionId);
        if (messages.isEmpty()) {
            throw new BusinessException("面试记录为空");
        }

        JobPosition job = getJob(session.getJobId());
        Resume resume = session.getResumeId() == null ? null : resumeService.getById(session.getResumeId());
        String prompt = buildReportPrompt(job, resume, messages);
        AiSecurityUtils.checkPromptLength(prompt);
        aiLimitService.checkAndIncrement(loginUser.getUserId());
        String result = callChatModelAndLog(loginUser.getUserId(), INTERVIEW_REPORT, prompt);

        AiInterviewReportVO vo = parseReportResult(result);
        vo.setSessionId(sessionId);

        AiInterviewSession update = new AiInterviewSession();
        update.setId(sessionId);
        update.setTotalScore(vo.getTotalScore());
        update.setReport(vo.getReport());
        update.setStatus(SESSION_FINISHED);
        update.setUpdateTime(LocalDateTime.now());
        aiInterviewSessionMapper.updateById(update);

        aiChatMemoryMapper.deleteById(sessionId);
        return vo;
    }

    private LoginUserDTO checkSeeker() {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录");
        }
        if (loginUser.getRole() == null || loginUser.getRole() != SEEKER_ROLE) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }
        return loginUser;
    }

    private Resume getOwnedResume(Long resumeId, Long userId) {
        Resume resume = resumeService.getById(resumeId);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        if (!userId.equals(resume.getUserId())) {
            throw new BusinessException("无权限操作该简历");
        }
        return resume;
    }

    private JobPosition getJob(Long jobId) {
        JobPosition job = jobPositionService.getById(jobId);
        if (job == null) {
            throw new BusinessException("岗位不存在");
        }
        return job;
    }

    private AiInterviewSession getOwnedSession(Long sessionId, Long userId) {
        if (sessionId == null) {
            throw new BusinessException("面试会话ID不能为空");
        }
        AiInterviewSession session = aiInterviewSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("面试会话不存在");
        }
        if (!userId.equals(session.getUserId())) {
            throw new BusinessException("无权限操作该面试会话");
        }
        return session;
    }

    private AiInterviewSession getOwnedRunningSession(Long sessionId, Long userId) {
        AiInterviewSession session = getOwnedSession(sessionId, userId);
        if (!Integer.valueOf(SESSION_RUNNING).equals(session.getStatus())) {
            throw new BusinessException("面试会话已完成");
        }
        return session;
    }

    private List<AiInterviewMessage> listAllMessages(Long sessionId) {
        return aiInterviewMessageMapper.selectList(new LambdaQueryWrapper<AiInterviewMessage>()
                .eq(AiInterviewMessage::getSessionId, sessionId)
                .orderByAsc(AiInterviewMessage::getCreateTime));
    }

    private int countUserAnswers(Long sessionId) {
        Long count = aiInterviewMessageMapper.selectCount(new LambdaQueryWrapper<AiInterviewMessage>()
                .eq(AiInterviewMessage::getSessionId, sessionId)
                .eq(AiInterviewMessage::getRole, ROLE_USER));
        return count == null ? 0 : count.intValue();
    }

    private void updateSessionStatus(Long sessionId, int status) {
        aiInterviewSessionMapper.update(null, new LambdaUpdateWrapper<AiInterviewSession>()
                .eq(AiInterviewSession::getId, sessionId)
                .set(AiInterviewSession::getStatus, status)
                .set(AiInterviewSession::getUpdateTime, LocalDateTime.now()));
    }

    private String callChatModelAndLog(Long userId, String module, String prompt) {
        String result;
        try {
            result = chatModel.chat(prompt);
        } catch (Exception e) {
            log.error("LangChain4j AI调用失败 module={}", module, e);
            saveLog(userId, module, prompt, AI_ERROR_MESSAGE);
            throw new BusinessException(AI_ERROR_MESSAGE);
        }
        if (!StringUtils.hasText(result)) {
            saveLog(userId, module, prompt, AI_ERROR_MESSAGE);
            throw new BusinessException(AI_ERROR_MESSAGE);
        }
        saveLog(userId, module, prompt, result);
        return result;
    }

    private void saveLog(Long userId, String module, String prompt, String result) {
        AiLogMessage message = new AiLogMessage();
        message.setUserId(userId);
        message.setModule(module);
        message.setPrompt(prompt);
        message.setResult(result);
        message.setCreateTime(LocalDateTime.now());
        if (rabbitMQProducer.sendAiLog(message)) {
            return;
        }
        try {
            AiCallLog logRecord = new AiCallLog();
            logRecord.setUserId(userId);
            logRecord.setModule(module);
            logRecord.setPrompt(prompt);
            logRecord.setResult(result);
            logRecord.setCreateTime(message.getCreateTime());
            aiCallLogMapper.insert(logRecord);
        } catch (Exception e) {
            log.error("LangChain4j AI日志保存失败", e);
        }
    }

    private AiJobMatchVO parseJobMatchResult(String result) {
        try {
            return objectMapper.readValue(extractJson(result), AiJobMatchVO.class);
        } catch (Exception e) {
            log.warn("岗位匹配结果JSON解析失败 result={}", result);
            AiJobMatchVO vo = new AiJobMatchVO();
            vo.setMatchScore(0);
            vo.setMatchLevel("暂无法评估");
            vo.setAdvantages(new ArrayList<>());
            vo.setWeaknesses(new ArrayList<>());
            vo.setSuggestions(List.of("AI 返回格式异常，请参考总体评价手动判断"));
            vo.setSummary(StringUtils.hasText(result) ? cleanPlainText(result) : "AI 返回格式异常");
            return vo;
        }
    }

    private AiInterviewAnswerResultVO parseAnswerResult(String result) {
        try {
            AiInterviewAnswerResultVO vo = objectMapper.readValue(extractJson(result), AiInterviewAnswerResultVO.class);
            vo.setScore(normalizeScore(vo.getScore()));
            vo.setFinished(Boolean.TRUE.equals(vo.getFinished()));
            return vo;
        } catch (Exception e) {
            log.warn("面试回答评分结果JSON解析失败 result={}", result);
            AiInterviewAnswerResultVO vo = new AiInterviewAnswerResultVO();
            vo.setScore(0);
            vo.setComment(cleanPlainText(result));
            vo.setSuggestion("AI 返回格式异常，请参考评价内容继续完善回答");
            vo.setFollowUpQuestion("");
            vo.setFinished(false);
            return vo;
        }
    }

    private AiInterviewReportVO parseReportResult(String result) {
        try {
            AiInterviewReportVO vo = objectMapper.readValue(extractJson(result), AiInterviewReportVO.class);
            vo.setTotalScore(normalizeScore(vo.getTotalScore()));
            return vo;
        } catch (Exception e) {
            log.warn("面试报告JSON解析失败 result={}", result);
            AiInterviewReportVO vo = new AiInterviewReportVO();
            vo.setTotalScore(0);
            vo.setLevel("暂无法评估");
            vo.setStrengths(new ArrayList<>());
            vo.setWeaknesses(new ArrayList<>());
            vo.setSuggestions(List.of("AI 返回格式异常，请参考完整总结手动复盘"));
            vo.setReport(StringUtils.hasText(result) ? cleanPlainText(result) : "AI 返回格式异常");
            return vo;
        }
    }

    private int normalizeScore(Integer score) {
        if (score == null) {
            return 0;
        }
        return Math.max(0, Math.min(100, score));
    }

    private String extractJson(String text) {
        if (!StringUtils.hasText(text)) {
            return "{}";
        }
        String result = text.trim()
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```", "")
                .trim();
        int start = result.indexOf('{');
        int end = result.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return result.substring(start, end + 1);
        }
        return result;
    }

    private String cleanPlainText(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.trim()
                .replace("```json", "")
                .replace("```markdown", "")
                .replace("```", "")
                .trim();
    }

    private String buildJobMatchPrompt(Resume resume, JobPosition job, Company company) {
        return "请根据以下简历和岗位信息，进行岗位匹配度分析。\n"
                + "请严格按 JSON 输出，不要输出 Markdown，不要输出多余解释。\n\n"
                + "JSON 格式：\n"
                + "{\n"
                + "  \"matchScore\": 85,\n"
                + "  \"matchLevel\": \"较匹配\",\n"
                + "  \"advantages\": [\"优势1\", \"优势2\"],\n"
                + "  \"weaknesses\": [\"不足1\", \"不足2\"],\n"
                + "  \"suggestions\": [\"建议1\", \"建议2\"],\n"
                + "  \"summary\": \"总体评价\"\n"
                + "}\n"
                + "评分要求：matchScore 为 0-100 整数，matchLevel 可选：非常匹配、较匹配、一般匹配、不太匹配。\n\n"
                + buildResumeInfo(resume)
                + "\n岗位信息：\n"
                + buildJobInfo(job, company);
    }

    private String buildInterviewSystemPrompt(JobPosition job, Resume resume) {
        return "你是资深 Java 后端面试官。请基于岗位和简历进行模拟面试，问题具体、有层次，不重复。\n\n"
                + buildJobInfo(job, null)
                + "\n"
                + (resume == null ? "未提供简历信息。\n" : buildResumeInfo(resume));
    }

    private String buildQuestionUserMessage(JobPosition job, Resume resume, int round) {
        return "请基于以下岗位和简历，结合我们前面的对话，给我第 " + round + " 道面试题（只输出题目本身，纯文本）。\n\n"
                + buildJobInfo(job, null)
                + "\n"
                + (resume == null ? "未提供简历信息。\n" : buildResumeInfo(resume));
    }

    private String buildAnswerUserMessage(JobPosition job, Resume resume, String answer, int answerCount) {
        return "请对我刚才的回答进行打分并给出追问。\n"
                + "当前已回答轮次：" + answerCount + "\n\n"
                + buildJobInfo(job, null)
                + "\n"
                + (resume == null ? "未提供简历信息。\n" : buildResumeInfo(resume))
                + "\n我的回答：\n" + answer;
    }

    private String buildReportPrompt(JobPosition job, Resume resume, List<AiInterviewMessage> messages) {
        return "请根据以下模拟面试完整记录，生成一份中文面试报告。\n"
                + "请严格按 JSON 输出，不要输出 Markdown，不要输出多余解释。\n\n"
                + "JSON 格式：\n"
                + "{\n"
                + "  \"totalScore\": 82,\n"
                + "  \"level\": \"表现良好\",\n"
                + "  \"strengths\": [\"优势1\", \"优势2\"],\n"
                + "  \"weaknesses\": [\"不足1\", \"不足2\"],\n"
                + "  \"suggestions\": [\"建议1\", \"建议2\"],\n"
                + "  \"report\": \"完整总结\"\n"
                + "}\n\n"
                + buildJobInfo(job, null)
                + "\n"
                + (resume == null ? "未提供简历信息。\n" : buildResumeInfo(resume))
                + "\n完整面试记录：\n"
                + buildMessagesText(messages);
    }

    private String buildResumeInfo(Resume resume) {
        return "简历信息：\n"
                + "标题：" + nullToEmpty(resume.getTitle()) + "\n"
                + "姓名：" + nullToEmpty(resume.getRealName()) + "\n"
                + "学历：" + nullToEmpty(resume.getEducation()) + "\n"
                + "工作年限：" + (resume.getExperienceYear() == null ? "" : resume.getExperienceYear()) + "\n"
                + "技能：" + nullToEmpty(resume.getSkill()) + "\n"
                + "项目经历：" + nullToEmpty(resume.getProjectExp()) + "\n"
                + "自我评价：" + nullToEmpty(resume.getSelfIntro()) + "\n";
    }

    private String buildJobInfo(JobPosition job, Company company) {
        return "岗位名称：" + nullToEmpty(job.getTitle()) + "\n"
                + "城市：" + nullToEmpty(job.getCity()) + "\n"
                + "薪资：" + value(job.getSalaryMin()) + "-" + value(job.getSalaryMax()) + "\n"
                + "经验要求：" + nullToEmpty(job.getExperience()) + "\n"
                + "学历要求：" + nullToEmpty(job.getEducation()) + "\n"
                + "技能标签：" + nullToEmpty(job.getTags()) + "\n"
                + "岗位描述：" + nullToEmpty(job.getDescription()) + "\n"
                + "公司名称：" + (company == null ? "" : nullToEmpty(company.getCompanyName())) + "\n"
                + "所属行业：" + (company == null ? "" : nullToEmpty(company.getIndustry())) + "\n";
    }

    private String buildMessagesText(List<AiInterviewMessage> messages) {
        StringBuilder builder = new StringBuilder();
        for (AiInterviewMessage message : messages) {
            builder.append(message.getRole()).append("：")
                    .append(nullToEmpty(message.getContent())).append("\n");
            if (message.getScore() != null) {
                builder.append("本轮评分：").append(message.getScore()).append("\n");
            }
            if (StringUtils.hasText(message.getSuggestion())) {
                builder.append("本轮建议：").append(message.getSuggestion()).append("\n");
            }
        }
        return builder.toString();
    }

    private AiInterviewSessionVO toSessionVO(AiInterviewSession session) {
        AiInterviewSessionVO vo = new AiInterviewSessionVO();
        vo.setId(session.getId());
        vo.setJobId(session.getJobId());
        vo.setResumeId(session.getResumeId());
        vo.setTitle(session.getTitle());
        vo.setStatus(session.getStatus());
        vo.setTotalScore(session.getTotalScore());
        vo.setReport(session.getReport());
        vo.setCreateTime(session.getCreateTime());
        vo.setUpdateTime(session.getUpdateTime());
        return vo;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String value(Integer value) {
        return value == null ? "" : value.toString();
    }
}
