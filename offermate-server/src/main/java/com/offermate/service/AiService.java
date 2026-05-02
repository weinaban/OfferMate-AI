package com.offermate.service;

import com.offermate.dto.AiInterviewQuestionsDTO;
import com.offermate.dto.AiJobDescriptionDTO;
import com.offermate.dto.AiResumeOptimizeFormDTO;
import com.offermate.dto.AiResumeOptimizeSectionDTO;
import com.offermate.dto.AiResumeOptimizeDTO;
import com.offermate.vo.AiResumeOptimizeFormVO;
import com.offermate.vo.AiResumeSectionOptimizeVO;
import com.offermate.vo.AiResultVO;

public interface AiService {

    AiResultVO optimizeResume(AiResumeOptimizeDTO dto);

    AiResumeOptimizeFormVO optimizeResumeForm(AiResumeOptimizeFormDTO dto);

    AiResumeSectionOptimizeVO optimizeResumeSection(AiResumeOptimizeSectionDTO dto);

    AiResultVO generateInterviewQuestions(AiInterviewQuestionsDTO dto);

    AiResultVO generateJobDescription(AiJobDescriptionDTO dto);
}
