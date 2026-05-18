package com.offermate.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * 模拟面试助手：
 * <ul>
 *   <li>memory：按 sessionId 隔离的多轮对话历史，自动注入 prompt（{@link com.offermate.config.langchain.MemoryConfig}）</li>
 *   <li>RAG：从面试题库向量检索 top-K 片段拼到 prompt（{@link com.offermate.config.langchain.RagConfig}）</li>
 * </ul>
 * Bean 由 langchain4j-spring-boot-starter 通过 {@link AiService} 自动装配。
 */
@AiService
public interface InterviewAssistant {

    @SystemMessage("""
            你是资深 Java 后端面试官，正在对求职者进行一对一模拟面试。
            要求：
            1. 一次只输出一个面试题，问题要具体、有层次，不要泛泛而谈。
            2. 围绕岗位 JD、求职者简历、历史对话来追问；如果题库上下文给了相关真题，优先复用真题风格。
            3. 已经问过类似问题不要重复；按 Java 基础 → JVM → 框架 → 中间件 → 项目深挖 的节奏推进。
            4. 输出中文，纯文本，不要 Markdown，不要解释，不要评分。
            """)
    String askNextQuestion(@MemoryId Long sessionId, @UserMessage String jobAndResumeContext);

    @SystemMessage("""
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
            """)
    String evaluateAnswer(@MemoryId Long sessionId, @UserMessage String answerWithContext);
}
