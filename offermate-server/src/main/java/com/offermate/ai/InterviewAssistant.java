package com.offermate.ai;

import com.offermate.config.ai.PersistentChatMemory;
import com.offermate.config.ai.RagConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟面试助手：
 * <ul>
 *   <li>memory：按 sessionId 隔离的多轮对话历史，从 PersistentChatMemory 读取与维护</li>
 *   <li>RAG：从面试题库向量检索 top-K 片段拼到 prompt（{@link RagConfig}）</li>
 *   <li>model：智谱 GLM-4-flash (ChatModel) 原生调用</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewAssistant {

    private static final String SYSTEM_PROMPT_QUESTION = """
            你是资深 Java 后端面试官，正在对求职者进行一对一模拟面试。
            要求：
            1. 一次只输出一个面试题，问题要具体、有层次，不要泛泛而谈。
            2. 围绕岗位 JD、求职者简历、历史对话来追问；如果题库上下文给了相关真题，优先复用真题风格。
            3. 已经问过类似问题不要重复；按 Java 基础 → JVM → 框架 → 中间件 → 项目深挖 的节奏推进。
            4. 输出中文，纯文本，不要 Markdown，不要解释，不要评分。
            """;

    private static final String SYSTEM_PROMPT_EVALUATION = """
            你是资深 Java 后端面试官，正在对求职者上一轮的回答进行评分。
            请严格按 JSON 输出，不要输出 Markdown，不要输出任何额外解释。
            JSON 格式：
            {
              "score": 78,
              "comment": "评价",
              "suggestion": "改进建议",
              "followUpQuestion": "追问问题",
              "finished": false
            }
            约束：
            - score 为 0-100 整数。
            - comment 简要评价回答质量，suggestion 给出可执行改进建议。
            - 如果已经完成 5 轮以上有效问答，可以 finished = true。
            - 题库上下文里若有相关参考答案，请结合参考答案打分。
            """;

    private final ChatModel chatModel;
    private final PersistentChatMemory chatMemory;
    private final RagConfig ragConfig;

    public String askNextQuestion(Long sessionId, String jobAndResumeContext) {
        String conversationId = String.valueOf(sessionId);
        String ragContext = ragConfig.retrieveContext(jobAndResumeContext);

        StringBuilder promptText = new StringBuilder();
        if (StringUtils.hasText(ragContext)) {
            promptText.append("【相关面试题库参考资料】：\n").append(ragContext).append("\n\n");
        }
        promptText.append(jobAndResumeContext);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT_QUESTION));

        List<Message> history = chatMemory.get(conversationId, 30);
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }

        UserMessage userMsg = new UserMessage(promptText.toString());
        messages.add(userMsg);

        ChatResponse response = chatModel.call(new Prompt(messages));
        String result = (response != null && response.getResult() != null && response.getResult().getOutput() != null)
                ? response.getResult().getOutput().getContent() : "";

        chatMemory.add(conversationId, List.of(userMsg, new AssistantMessage(result)));
        return result;
    }

    public String evaluateAnswer(Long sessionId, String answerWithContext) {
        String conversationId = String.valueOf(sessionId);
        String ragContext = ragConfig.retrieveContext(answerWithContext);

        StringBuilder promptText = new StringBuilder();
        if (StringUtils.hasText(ragContext)) {
            promptText.append("【相关面试题与参考答案】：\n").append(ragContext).append("\n\n");
        }
        promptText.append(answerWithContext);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT_EVALUATION));

        List<Message> history = chatMemory.get(conversationId, 30);
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }

        UserMessage userMsg = new UserMessage(promptText.toString());
        messages.add(userMsg);

        ChatResponse response = chatModel.call(new Prompt(messages));
        String result = (response != null && response.getResult() != null && response.getResult().getOutput() != null)
                ? response.getResult().getOutput().getContent() : "";

        chatMemory.add(conversationId, List.of(userMsg, new AssistantMessage(result)));
        return result;
    }
}
