package com.offermate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offermate.dto.InterviewCreateDTO;
import com.offermate.entity.InterviewInvitation;
import com.offermate.vo.InterviewInvitationVO;

import java.util.List;

public interface InterviewInvitationService extends IService<InterviewInvitation> {

    void createInterview(InterviewCreateDTO dto);

    List<InterviewInvitationVO> listMyInterviews();

    List<InterviewInvitationVO> listCompanyInterviews();

    void acceptInterview(Long id);

    void rejectInterview(Long id);

    void cancelInterview(Long id);
}
