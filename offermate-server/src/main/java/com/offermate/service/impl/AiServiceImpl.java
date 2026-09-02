package com.offermate.service.impl;

import com.offermate.common.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offermate.dto.AiInterviewQuestionsDTO;
import com.offermate.dto.AiJobDescriptionDTO;
import com.offermate.dto.AiResumeOptimizeFormDTO;
import com.offermate.dto.AiResumeOptimizeSectionDTO;
import com.offermate.dto.AiResumeOptimizeDTO;
import com.offermate.dto.LoginUserDTO;
import com.offermate.entity.AiCallLog;
import com.offermate.entity.JobPosition;
import com.offermate.entity.Resume;
import com.offermate.exception.BusinessException;
import com.offermate.mapper.AiCallLogMapper;
import com.offermate.mq.RabbitMQProducer;
import com.offermate.mq.dto.AiLogMessage;
import com.offermate.service.AiLimitService;
import com.offermate.service.AiService;
import com.offermate.service.JobPositionService;
import com.offermate.service.ResumeService;
import com.offermate.util.AiSecurityUtils;
import com.offermate.util.UserContext;
import com.offermate.vo.AiResumeOptimizeFormVO;
import com.offermate.vo.AiResumeSectionOptimizeVO;
import com.offermate.vo.AiResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private static final int SEEKER_ROLE = 1;
    private static final int RECRUITER_ROLE = 2;
    private static final String AI_ERROR_MESSAGE = "AI 服务暂时不可用，请稍后再试";
    private static final String RESUME_OPTIMIZE = "RESUME_OPTIMIZE";
    private static final String RESUME_SECTION_OPTIMIZE = "RESUME_SECTION_OPTIMIZE";
    private static final String INTERVIEW_QUESTIONS = "INTERVIEW_QUESTIONS";
    private static final String JOB_DESCRIPTION = "JOB_DESCRIPTION";
    private static final String SECTION_SKILL = "skill";
    private static final String SECTION_PROJECT_EXP = "projectExp";
    private static final String SECTION_SELF_INTRO = "selfIntro";

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final AiCallLogMapper aiCallLogMapper;
    private final ResumeService resumeService;
    private final JobPositionService jobPositionService;
    private final AiLimitService aiLimitService;
    private final RabbitMQProducer rabbitMQProducer;

    @Override
    public AiResultVO optimizeResume(AiResumeOptimizeDTO dto) {
        LoginUserDTO loginUser = checkRole(SEEKER_ROLE);
        if (dto == null || dto.getResumeId() == null) {
            throw new BusinessException("简历ID不能为空");
        }

        Resume resume = resumeService.getById(dto.getResumeId());
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        if (!resume.getUserId().equals(loginUser.getUserId())) {
            throw new BusinessException("无权限操作该简历");
        }

        JobPosition job = null;
        if (dto.getJobId() != null) {
            job = jobPositionService.getById(dto.getJobId());
            if (job == null) {
                throw new BusinessException("岗位不存在");
            }
        }

        String prompt = buildResumePrompt(resume, job);
        AiSecurityUtils.checkPromptLength(prompt);
        aiLimitService.checkAndIncrement(loginUser.getUserId());
        String result = callAi(prompt);
        saveLog(loginUser.getUserId(), RESUME_OPTIMIZE, prompt, result);
        checkAiResult(result);
        return new AiResultVO(result);
    }

    @Override
    public AiResumeOptimizeFormVO optimizeResumeForm(AiResumeOptimizeFormDTO dto) {
        LoginUserDTO loginUser = checkRole(SEEKER_ROLE);
        if (dto == null) {
            dto = new AiResumeOptimizeFormDTO();
        }

        JobPosition job = null;
        if (dto.getJobId() != null) {
            job = jobPositionService.getById(dto.getJobId());
            if (job == null) {
                throw new BusinessException("岗位不存在");
            }
        }

        String prompt = buildResumeFormPrompt(dto, job);
        AiSecurityUtils.checkPromptLength(prompt);
        aiLimitService.checkAndIncrement(loginUser.getUserId());
        String result = callAi(prompt);
        saveLog(loginUser.getUserId(), RESUME_OPTIMIZE, prompt, result);
        checkAiResult(result);
        return parseResumeFormResult(result, dto);
    }

    @Override
    public AiResumeSectionOptimizeVO optimizeResumeSection(AiResumeOptimizeSectionDTO dto) {
        LoginUserDTO loginUser = checkRole(SEEKER_ROLE);
        if (dto == null) {
            throw new BusinessException("请求参数不能为空");
        }
        String section = dto.getSection();
        if (!StringUtils.hasText(section)) {
            throw new BusinessException("优化模块不能为空");
        }
        if (!isValidSection(section)) {
            throw new BusinessException("优化模块不合法");
        }

        if (dto.getResumeId() != null) {
            Resume resume = resumeService.getById(dto.getResumeId());
            if (resume == null) {
                throw new BusinessException("简历不存在");
            }
            if (!resume.getUserId().equals(loginUser.getUserId())) {
                throw new BusinessException("无权限操作该简历");
            }
        }

        JobPosition job = null;
        if (dto.getJobId() != null) {
            job = jobPositionService.getById(dto.getJobId());
            if (job == null) {
                throw new BusinessException("岗位不存在");
            }
        }

        String prompt = buildResumeSectionPrompt(dto, job);
        AiSecurityUtils.checkPromptLength(prompt);
        aiLimitService.checkAndIncrement(loginUser.getUserId());
        String result = callAi(prompt);
        saveLog(loginUser.getUserId(), RESUME_SECTION_OPTIMIZE, prompt, result);
        checkAiResult(result);

        AiResumeSectionOptimizeVO vo = new AiResumeSectionOptimizeVO();
        vo.setContent(cleanSectionContent(result, section));
        return vo;
    }

    @Override
    public AiResultVO generateInterviewQuestions(AiInterviewQuestionsDTO dto) {
        LoginUserDTO loginUser = getLoginUser();
        if (loginUser.getRole() == null || (loginUser.getRole() != SEEKER_ROLE && loginUser.getRole() != RECRUITER_ROLE)) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }
        if (dto == null || dto.getJobId() == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        int count = dto.getCount() == null ? 5 : dto.getCount();
        if (count < 3) {
            count = 3;
        }
        if (count > 10) {
            count = 10;
        }

        JobPosition job = jobPositionService.getById(dto.getJobId());
        if (job == null) {
            throw new BusinessException("岗位不存在");
        }

        String prompt = buildInterviewPrompt(job, count);
        AiSecurityUtils.checkPromptLength(prompt);
        aiLimitService.checkAndIncrement(loginUser.getUserId());
        String result = callAi(prompt);
        saveLog(loginUser.getUserId(), INTERVIEW_QUESTIONS, prompt, result);
        checkAiResult(result);
        return new AiResultVO(result);
    }

    @Override
    public AiResultVO generateJobDescription(AiJobDescriptionDTO dto) {
        LoginUserDTO loginUser = checkRole(RECRUITER_ROLE);
        if (dto == null || !StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException("岗位名称不能为空");
        }

        String prompt = buildJobDescriptionPrompt(dto);
        AiSecurityUtils.checkPromptLength(prompt);
        aiLimitService.checkAndIncrement(loginUser.getUserId());
        String result = callAi(prompt);
        saveLog(loginUser.getUserId(), JOB_DESCRIPTION, prompt, result);
        checkAiResult(result);
        return new AiResultVO(result);
    }

    private LoginUserDTO checkRole(int role) {
        LoginUserDTO loginUser = getLoginUser();
        if (loginUser.getRole() == null || loginUser.getRole() != role) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }
        return loginUser;
    }

    private LoginUserDTO getLoginUser() {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录");
        }
        return loginUser;
    }

    private String callAi(String prompt) {
        try {
            String content = chatModel.call(prompt);
            if (!StringUtils.hasText(content)) {
                return AI_ERROR_MESSAGE;
            }
            return content;
        } catch (Exception e) {
            log.error("Spring AI 调用失败", e);
            return AI_ERROR_MESSAGE;
        }
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
            log.error("AI调用日志保存失败", e);
        }
    }

    private void checkAiResult(String result) {
        if (AI_ERROR_MESSAGE.equals(result)) {
            throw new BusinessException(AI_ERROR_MESSAGE);
        }
    }

    private AiResumeOptimizeFormVO parseResumeFormResult(String result, AiResumeOptimizeFormDTO dto) {
        String jsonText = cleanJsonText(result);
        try {
            JsonNode root = objectMapper.readTree(jsonText);
            AiResumeOptimizeFormVO vo = new AiResumeOptimizeFormVO();
            vo.setTitle(getJsonText(root, "title", dto.getTitle()));
            vo.setSkill(getJsonText(root, "skill", dto.getSkill()));
            vo.setProjectExp(getJsonText(root, "projectExp", dto.getProjectExp()));
            vo.setSelfIntro(getJsonText(root, "selfIntro", dto.getSelfIntro()));
            vo.setSuggestion(getJsonText(root, "suggestion", "暂无额外建议"));
            return vo;
        } catch (Exception e) {
            log.warn("AI简历表单优化结果解析失败，返回兜底数据");
            AiResumeOptimizeFormVO vo = new AiResumeOptimizeFormVO();
            vo.setTitle(nullToEmpty(dto.getTitle()));
            vo.setSkill(nullToEmpty(dto.getSkill()));
            vo.setProjectExp(nullToEmpty(dto.getProjectExp()));
            vo.setSelfIntro(nullToEmpty(dto.getSelfIntro()));
            vo.setSuggestion(StringUtils.hasText(result) ? result : "AI 返回格式异常，请参考原始建议手动修改");
            return vo;
        }
    }

    private String cleanJsonText(String text) {
        if (!StringUtils.hasText(text)) {
            return "{}";
        }
        String result = text.trim();
        if (result.startsWith("```")) {
            result = result.replaceFirst("^```[a-zA-Z]*\\s*", "");
            result = result.replaceFirst("\\s*```$", "");
        }
        int start = result.indexOf('{');
        int end = result.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return result.substring(start, end + 1);
        }
        return result;
    }

    private String getJsonText(JsonNode root, String fieldName, String defaultValue) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            return nullToEmpty(defaultValue);
        }
        String value = node.asText();
        return StringUtils.hasText(value) ? value : nullToEmpty(defaultValue);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean isValidSection(String section) {
        return SECTION_SKILL.equals(section)
                || SECTION_PROJECT_EXP.equals(section)
                || SECTION_SELF_INTRO.equals(section);
    }

    private String cleanSectionContent(String text, String section) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String result = text.trim()
                .replace("```json", "")
                .replace("```markdown", "")
                .replace("```", "")
                .trim();

        result = extractSectionContent(result, section);
        result = removeSectionTitle(result, section);
        result = result.replaceAll("(?m)^\\s*[-*#]+\\s*", "").trim();
        return result;
    }

    private String extractSectionContent(String text, String section) {
        String currentTitle = getSectionTitle(section);
        int start = indexOfTitle(text, currentTitle);
        if (start >= 0) {
            String result = text.substring(start + currentTitle.length());
            result = result.replaceFirst("^\\s*[:：]\\s*", "");
            return cutBeforeOtherTitle(result, section);
        }
        return cutBeforeOtherTitle(text, section);
    }

    private String cutBeforeOtherTitle(String text, String section) {
        int end = -1;
        for (String title : new String[]{"技能描述", "技能", "项目经历", "项目经验", "自我评价", "个人评价", "标题", "简历标题"}) {
            if (title.equals(getSectionTitle(section))) {
                continue;
            }
            int index = indexOfTitle(text, title);
            if (index > 0 && (end == -1 || index < end)) {
                end = index;
            }
        }
        return end > 0 ? text.substring(0, end).trim() : text.trim();
    }

    private int indexOfTitle(String text, String title) {
        int index = text.indexOf(title + "：");
        if (index < 0) {
            index = text.indexOf(title + ":");
        }
        return index;
    }

    private String removeSectionTitle(String text, String section) {
        String title = getSectionTitle(section);
        return text.replaceFirst("^\\s*" + title + "\\s*[:：]\\s*", "")
                .replaceFirst("^\\s*" + getFallbackSectionTitle(section) + "\\s*[:：]\\s*", "")
                .trim();
    }

    private String getSectionTitle(String section) {
        if (SECTION_SKILL.equals(section)) {
            return "技能描述";
        }
        if (SECTION_PROJECT_EXP.equals(section)) {
            return "项目经历";
        }
        return "自我评价";
    }

    private String getFallbackSectionTitle(String section) {
        if (SECTION_SKILL.equals(section)) {
            return "技能";
        }
        if (SECTION_PROJECT_EXP.equals(section)) {
            return "项目经验";
        }
        return "个人评价";
    }

    private String buildResumeSectionPrompt(AiResumeOptimizeSectionDTO dto, JobPosition job) {
        String section = dto.getSection();
        String sectionName = getSectionTitle(section);
        String currentContent = getCurrentSectionContent(dto);
        StringBuilder prompt = new StringBuilder();
        prompt.append("请基于以下简历表单内容，只优化").append(sectionName).append("。\n")
                .append("要求：\n")
                .append("1. 只输出优化后的").append(sectionName).append("内容。\n")
                .append("2. 不要输出标题，不要输出 Markdown 表格，不要输出代码块。\n")
                .append("3. 不要输出技能描述、项目经历、自我评价之外的其他模块。\n")
                .append("4. 不要把三个模块混在一起。\n")
                .append("5. 输出中文，可以在原内容基础上润色、结构化、增强表达。\n")
                .append("6. 不要编造离谱经历，返回内容控制在 300-800 字以内。\n");
        appendSectionRule(prompt, section);
        prompt.append("\n简历基础信息：\n")
                .append("简历标题：").append(nullToEmpty(dto.getTitle())).append("\n")
                .append("姓名：").append(nullToEmpty(dto.getRealName())).append("\n")
                .append("电话：").append(nullToEmpty(dto.getPhone())).append("\n")
                .append("邮箱：").append(nullToEmpty(dto.getEmail())).append("\n")
                .append("学历：").append(nullToEmpty(dto.getEducation())).append("\n")
                .append("工作年限：").append(dto.getExperienceYear() == null ? "" : dto.getExperienceYear()).append("\n")
                .append("当前技能描述：").append(nullToEmpty(dto.getSkill())).append("\n")
                .append("当前项目经历：").append(nullToEmpty(dto.getProjectExp())).append("\n")
                .append("当前自我评价：").append(nullToEmpty(dto.getSelfIntro())).append("\n")
                .append("\n本次需要优化的原始内容：\n")
                .append(currentContent).append("\n");
        if (job != null) {
            prompt.append("\n目标岗位信息：\n")
                    .append("岗位名称：").append(job.getTitle()).append("\n")
                    .append("城市：").append(job.getCity()).append("\n")
                    .append("薪资：").append(job.getSalaryMin()).append("-").append(job.getSalaryMax()).append("\n")
                    .append("经验要求：").append(job.getExperience()).append("\n")
                    .append("学历要求：").append(job.getEducation()).append("\n")
                    .append("技能标签：").append(job.getTags()).append("\n")
                    .append("岗位描述：").append(job.getDescription()).append("\n");
        }
        return prompt.toString();
    }

    private void appendSectionRule(StringBuilder prompt, String section) {
        if (SECTION_SKILL.equals(section)) {
            prompt.append("7. 只优化技能描述，不要输出项目经历、自我评价或标题。\n");
            return;
        }
        if (SECTION_PROJECT_EXP.equals(section)) {
            prompt.append("7. 只优化项目经历，不要输出技能描述、自我评价或标题。\n");
            return;
        }
        prompt.append("7. 只优化自我评价，不要输出技能描述、项目经历或标题。\n");
    }

    private String getCurrentSectionContent(AiResumeOptimizeSectionDTO dto) {
        if (SECTION_SKILL.equals(dto.getSection())) {
            return nullToEmpty(dto.getSkill());
        }
        if (SECTION_PROJECT_EXP.equals(dto.getSection())) {
            return nullToEmpty(dto.getProjectExp());
        }
        return nullToEmpty(dto.getSelfIntro());
    }

    private String buildResumeFormPrompt(AiResumeOptimizeFormDTO dto, JobPosition job) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下简历表单内容，优化求职简历。\n")
                .append("要求：\n")
                .append("1. 只返回 JSON 对象，不要 Markdown，不要解释文字，不要 ```json 代码块。\n")
                .append("2. JSON 字段必须固定为：\n")
                .append("{\n")
                .append("  \"title\": \"...\",\n")
                .append("  \"skill\": \"...\",\n")
                .append("  \"projectExp\": \"...\",\n")
                .append("  \"selfIntro\": \"...\",\n")
                .append("  \"suggestion\": \"...\"\n")
                .append("}\n")
                .append("3. 内容使用中文。\n")
                .append("4. skill / projectExp / selfIntro 要适合直接回填到前端 textarea。\n")
                .append("5. 不要编造过度夸张经历，可以在原内容基础上润色、结构化、补充表达。\n\n")
                .append("简历表单内容：\n")
                .append("简历标题：").append(nullToEmpty(dto.getTitle())).append("\n")
                .append("姓名：").append(nullToEmpty(dto.getRealName())).append("\n")
                .append("电话：").append(nullToEmpty(dto.getPhone())).append("\n")
                .append("邮箱：").append(nullToEmpty(dto.getEmail())).append("\n")
                .append("学历：").append(nullToEmpty(dto.getEducation())).append("\n")
                .append("工作年限：").append(dto.getExperienceYear() == null ? "" : dto.getExperienceYear()).append("\n")
                .append("技能：").append(nullToEmpty(dto.getSkill())).append("\n")
                .append("项目经历：").append(nullToEmpty(dto.getProjectExp())).append("\n")
                .append("自我评价：").append(nullToEmpty(dto.getSelfIntro())).append("\n");
        if (job != null) {
            prompt.append("\n目标岗位信息：\n")
                    .append("岗位名称：").append(job.getTitle()).append("\n")
                    .append("城市：").append(job.getCity()).append("\n")
                    .append("薪资：").append(job.getSalaryMin()).append("-").append(job.getSalaryMax()).append("\n")
                    .append("经验要求：").append(job.getExperience()).append("\n")
                    .append("学历要求：").append(job.getEducation()).append("\n")
                    .append("技能标签：").append(job.getTags()).append("\n")
                    .append("岗位描述：").append(job.getDescription()).append("\n");
        }
        return prompt.toString();
    }

    private String buildResumePrompt(Resume resume, JobPosition job) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下简历内容生成专业、具体、可落地的优化建议。\n")
                .append("要求：\n")
                .append("1. 从简历标题、技能、项目经历、自我评价等角度分析\n")
                .append("2. 指出问题\n")
                .append("3. 给出修改建议\n")
                .append("4. 如果传入岗位信息，请结合岗位要求进行针对性优化\n")
                .append("5. 输出中文\n")
                .append("6. 结构清晰，适合直接展示给求职者\n\n")
                .append("简历信息：\n")
                .append("简历标题：").append(resume.getTitle()).append("\n")
                .append("姓名：").append(resume.getRealName()).append("\n")
                .append("学历：").append(resume.getEducation()).append("\n")
                .append("工作年限：").append(resume.getExperienceYear()).append("\n")
                .append("技能：").append(resume.getSkill()).append("\n")
                .append("项目经历：").append(resume.getProjectExp()).append("\n")
                .append("自我评价：").append(resume.getSelfIntro()).append("\n");
        if (job != null) {
            prompt.append("\n岗位信息：\n")
                    .append("岗位名称：").append(job.getTitle()).append("\n")
                    .append("薪资：").append(job.getSalaryMin()).append("-").append(job.getSalaryMax()).append("\n")
                    .append("城市：").append(job.getCity()).append("\n")
                    .append("经验要求：").append(job.getExperience()).append("\n")
                    .append("学历要求：").append(job.getEducation()).append("\n")
                    .append("技能标签：").append(job.getTags()).append("\n")
                    .append("岗位描述：").append(job.getDescription()).append("\n");
        }
        return prompt.toString();
    }

    private String buildInterviewPrompt(JobPosition job, int count) {
        return "请根据以下岗位信息生成模拟面试题。\n"
                + "要求：\n"
                + "1. 输出 " + count + " 道题\n"
                + "2. 包含技术基础题、项目经验题、场景题\n"
                + "3. 每道题给出考察点\n"
                + "4. 如果是 Java/Spring Boot 相关岗位，要体现后端项目和数据库相关问题\n"
                + "5. 输出中文\n"
                + "6. 结构清晰，适合前端直接展示\n\n"
                + "岗位信息：\n"
                + "岗位名称：" + job.getTitle() + "\n"
                + "城市：" + job.getCity() + "\n"
                + "薪资：" + job.getSalaryMin() + "-" + job.getSalaryMax() + "\n"
                + "经验要求：" + job.getExperience() + "\n"
                + "学历要求：" + job.getEducation() + "\n"
                + "技能标签：" + job.getTags() + "\n"
                + "岗位描述：" + job.getDescription();
    }

    private String buildJobDescriptionPrompt(AiJobDescriptionDTO dto) {
        return "请根据以下岗位基础信息生成一份专业的招聘岗位描述。\n"
                + "要求：\n"
                + "1. 包含岗位职责\n"
                + "2. 包含任职要求\n"
                + "3. 包含加分项\n"
                + "4. 包含岗位亮点\n"
                + "5. 语言风格参考 Boss 直聘招聘岗位描述\n"
                + "6. 不要夸张宣传\n"
                + "7. 输出中文\n"
                + "8. 结果可以直接作为岗位 description 使用\n\n"
                + "岗位信息：\n"
                + "岗位名称：" + dto.getTitle() + "\n"
                + "城市：" + dto.getCity() + "\n"
                + "薪资：" + dto.getSalaryMin() + "-" + dto.getSalaryMax() + "\n"
                + "经验要求：" + dto.getExperience() + "\n"
                + "学历要求：" + dto.getEducation() + "\n"
                + "技能标签：" + dto.getTags() + "\n"
                + "公司名称：" + dto.getCompanyName() + "\n"
                + "所属行业：" + dto.getIndustry() + "\n"
                + "公司规模：" + dto.getScale();
    }
}
