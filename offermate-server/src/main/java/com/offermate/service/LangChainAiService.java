package com.offermate.service;

import com.offermate.dto.AiInterviewAnswerDTO;
import com.offermate.dto.AiInterviewSessionCreateDTO;
import com.offermate.dto.AiJobMatchDTO;
import com.offermate.vo.AiInterviewAnswerResultVO;
import com.offermate.vo.AiInterviewQuestionVO;
import com.offermate.vo.AiInterviewReportVO;
import com.offermate.vo.AiInterviewSessionVO;
import com.offermate.vo.AiJobMatchVO;

public interface LangChainAiService {

    AiJobMatchVO matchJob(AiJobMatchDTO dto);

    AiInterviewSessionVO createInterviewSession(AiInterviewSessionCreateDTO dto);

    AiInterviewQuestionVO generateQuestion(Long sessionId);

    AiInterviewAnswerResultVO submitAnswer(Long sessionId, AiInterviewAnswerDTO dto);

    AiInterviewReportVO generateReport(Long sessionId);
}
