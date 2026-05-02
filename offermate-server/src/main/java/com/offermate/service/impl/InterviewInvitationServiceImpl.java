package com.offermate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offermate.common.Result;
import com.offermate.dto.InterviewCreateDTO;
import com.offermate.dto.LoginUserDTO;
import com.offermate.entity.Company;
import com.offermate.entity.InterviewInvitation;
import com.offermate.entity.JobDelivery;
import com.offermate.entity.JobPosition;
import com.offermate.entity.Resume;
import com.offermate.entity.SysUser;
import com.offermate.exception.BusinessException;
import com.offermate.mapper.InterviewInvitationMapper;
import com.offermate.mq.RabbitMQProducer;
import com.offermate.mq.dto.NotificationMessage;
import com.offermate.service.CompanyService;
import com.offermate.service.InterviewInvitationService;
import com.offermate.service.JobDeliveryService;
import com.offermate.service.JobPositionService;
import com.offermate.service.ResumeService;
import com.offermate.service.SysUserService;
import com.offermate.util.UserContext;
import com.offermate.vo.InterviewInvitationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewInvitationServiceImpl extends ServiceImpl<InterviewInvitationMapper, InterviewInvitation>
        implements InterviewInvitationService {

    private static final int SEEKER_ROLE = 1;
    private static final int RECRUITER_ROLE = 2;
    private static final int DELIVERY_INTERVIEW_STATUS = 4;
    private static final int WAIT_CONFIRM_STATUS = 1;
    private static final int ACCEPTED_STATUS = 2;
    private static final int REJECTED_STATUS = 3;
    private static final int CANCELED_STATUS = 4;
    private static final int INTERVIEW_NOTICE_TYPE = 2;

    private final JobDeliveryService jobDeliveryService;
    private final JobPositionService jobPositionService;
    private final CompanyService companyService;
    private final ResumeService resumeService;
    private final SysUserService sysUserService;
    private final RabbitMQProducer rabbitMQProducer;

    @Override
    public void createInterview(InterviewCreateDTO dto) {
        LoginUserDTO loginUser = checkRole(RECRUITER_ROLE);
        if (dto == null || dto.getDeliveryId() == null) {
            throw new BusinessException("投递记录ID不能为空");
        }
        if (dto.getInterviewTime() == null) {
            throw new BusinessException("面试时间不能为空");
        }
        if (!dto.getInterviewTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("面试时间必须晚于当前时间");
        }

        JobDelivery delivery = jobDeliveryService.getById(dto.getDeliveryId());
        if (delivery == null) {
            throw new BusinessException("投递记录不存在");
        }

        JobPosition job = jobPositionService.getById(delivery.getJobId());
        if (job == null) {
            throw new BusinessException("岗位不存在");
        }
        if (!loginUser.getUserId().equals(job.getRecruiterId())) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }

        long exists = count(new LambdaQueryWrapper<InterviewInvitation>()
                .eq(InterviewInvitation::getDeliveryId, delivery.getId())
                .in(InterviewInvitation::getStatus, WAIT_CONFIRM_STATUS, ACCEPTED_STATUS));
        if (exists > 0) {
            throw new BusinessException("该投递已存在面试邀请");
        }

        InterviewInvitation invitation = new InterviewInvitation();
        invitation.setSeekerId(delivery.getUserId());
        invitation.setRecruiterId(loginUser.getUserId());
        invitation.setCompanyId(delivery.getCompanyId());
        invitation.setJobId(delivery.getJobId());
        invitation.setDeliveryId(delivery.getId());
        invitation.setInterviewTime(dto.getInterviewTime());
        invitation.setAddress(trim(dto.getAddress()));
        invitation.setContactName(trim(dto.getContactName()));
        invitation.setContactPhone(trim(dto.getContactPhone()));
        invitation.setRemark(trim(dto.getRemark()));
        invitation.setStatus(WAIT_CONFIRM_STATUS);
        save(invitation);

        if (!Integer.valueOf(DELIVERY_INTERVIEW_STATUS).equals(delivery.getStatus())) {
            delivery.setStatus(DELIVERY_INTERVIEW_STATUS);
            jobDeliveryService.updateById(delivery);
        }

        sendInterviewNotice(delivery.getUserId(), "面试邀请", buildJobContent("你收到一份新的面试邀请", job.getTitle()));
    }

    @Override
    public List<InterviewInvitationVO> listMyInterviews() {
        LoginUserDTO loginUser = checkRole(SEEKER_ROLE);
        List<InterviewInvitation> invitations = list(new LambdaQueryWrapper<InterviewInvitation>()
                .eq(InterviewInvitation::getSeekerId, loginUser.getUserId())
                .orderByDesc(InterviewInvitation::getCreateTime));
        return buildVOList(invitations);
    }

    @Override
    public List<InterviewInvitationVO> listCompanyInterviews() {
        LoginUserDTO loginUser = checkRole(RECRUITER_ROLE);
        List<InterviewInvitation> invitations = list(new LambdaQueryWrapper<InterviewInvitation>()
                .eq(InterviewInvitation::getRecruiterId, loginUser.getUserId())
                .orderByDesc(InterviewInvitation::getCreateTime));
        return buildVOList(invitations);
    }

    @Override
    public void acceptInterview(Long id) {
        InterviewInvitation invitation = checkSeekerInvitation(id);
        checkWaiting(invitation);
        invitation.setStatus(ACCEPTED_STATUS);
        updateById(invitation);
        sendInterviewNotice(invitation.getRecruiterId(), "面试邀请已接受", "求职者已接受你的面试邀请");
    }

    @Override
    public void rejectInterview(Long id) {
        InterviewInvitation invitation = checkSeekerInvitation(id);
        checkWaiting(invitation);
        invitation.setStatus(REJECTED_STATUS);
        updateById(invitation);
        sendInterviewNotice(invitation.getRecruiterId(), "面试邀请已拒绝", "求职者已拒绝你的面试邀请");
    }

    @Override
    public void cancelInterview(Long id) {
        LoginUserDTO loginUser = checkRole(RECRUITER_ROLE);
        InterviewInvitation invitation = getById(id);
        if (invitation == null) {
            throw new BusinessException("面试邀请不存在");
        }
        if (!invitation.getRecruiterId().equals(loginUser.getUserId())) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }
        if (!Integer.valueOf(WAIT_CONFIRM_STATUS).equals(invitation.getStatus())
                && !Integer.valueOf(ACCEPTED_STATUS).equals(invitation.getStatus())) {
            throw new BusinessException("当前状态不可操作");
        }
        invitation.setStatus(CANCELED_STATUS);
        updateById(invitation);
        sendInterviewNotice(invitation.getSeekerId(), "面试邀请已取消", "你的面试邀请已被企业取消");
    }

    private InterviewInvitation checkSeekerInvitation(Long id) {
        LoginUserDTO loginUser = checkRole(SEEKER_ROLE);
        InterviewInvitation invitation = getById(id);
        if (invitation == null) {
            throw new BusinessException("面试邀请不存在");
        }
        if (!invitation.getSeekerId().equals(loginUser.getUserId())) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }
        return invitation;
    }

    private void checkWaiting(InterviewInvitation invitation) {
        if (!Integer.valueOf(WAIT_CONFIRM_STATUS).equals(invitation.getStatus())) {
            throw new BusinessException("当前状态不可操作");
        }
    }

    private LoginUserDTO checkRole(int role) {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getRole() == null || loginUser.getRole() != role) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }
        return loginUser;
    }

    private void sendInterviewNotice(Long userId, String title, String content) {
        NotificationMessage message = new NotificationMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(INTERVIEW_NOTICE_TYPE);
        rabbitMQProducer.sendInterviewNotice(message);
    }

    private String buildJobContent(String content, String jobTitle) {
        if (!StringUtils.hasText(jobTitle)) {
            return content;
        }
        return content + "：【" + jobTitle + "】";
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private List<InterviewInvitationVO> buildVOList(List<InterviewInvitation> invitations) {
        if (invitations.isEmpty()) {
            return List.of();
        }

        Map<Long, JobPosition> jobMap = jobPositionService.listByIds(invitations.stream()
                        .map(InterviewInvitation::getJobId)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(JobPosition::getId, Function.identity(), (a, b) -> a));

        Map<Long, Company> companyMap = companyService.listByIds(invitations.stream()
                        .map(InterviewInvitation::getCompanyId)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Company::getId, Function.identity(), (a, b) -> a));

        Map<Long, SysUser> userMap = sysUserService.listByIds(invitations.stream()
                        .flatMap(invitation -> List.of(invitation.getSeekerId(), invitation.getRecruiterId()).stream())
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));

        Map<Long, Resume> resumeMap = resumeService.list(new LambdaQueryWrapper<Resume>()
                        .in(Resume::getUserId, invitations.stream()
                                .map(InterviewInvitation::getSeekerId)
                                .distinct()
                                .toList())
                        .orderByDesc(Resume::getIsDefault)
                        .orderByDesc(Resume::getCreateTime))
                .stream()
                .collect(Collectors.toMap(Resume::getUserId, Function.identity(), (a, b) -> a));

        return invitations.stream()
                .map(invitation -> toVO(invitation, jobMap, companyMap, userMap, resumeMap))
                .toList();
    }

    private InterviewInvitationVO toVO(InterviewInvitation invitation,
                                       Map<Long, JobPosition> jobMap,
                                       Map<Long, Company> companyMap,
                                       Map<Long, SysUser> userMap,
                                       Map<Long, Resume> resumeMap) {
        InterviewInvitationVO vo = new InterviewInvitationVO();
        vo.setId(invitation.getId());
        vo.setSeekerId(invitation.getSeekerId());
        vo.setRecruiterId(invitation.getRecruiterId());
        vo.setCompanyId(invitation.getCompanyId());
        vo.setJobId(invitation.getJobId());
        vo.setDeliveryId(invitation.getDeliveryId());
        vo.setInterviewTime(invitation.getInterviewTime());
        vo.setAddress(invitation.getAddress());
        vo.setContactName(invitation.getContactName());
        vo.setContactPhone(invitation.getContactPhone());
        vo.setRemark(invitation.getRemark());
        vo.setStatus(invitation.getStatus());
        vo.setCreateTime(invitation.getCreateTime());
        vo.setUpdateTime(invitation.getUpdateTime());

        JobPosition job = jobMap.get(invitation.getJobId());
        if (job != null) {
            vo.setJobTitle(job.getTitle());
        }
        Company company = companyMap.get(invitation.getCompanyId());
        if (company != null) {
            vo.setCompanyName(company.getCompanyName());
            vo.setCompanyLogo(company.getLogo());
        }
        SysUser recruiter = userMap.get(invitation.getRecruiterId());
        if (recruiter != null) {
            vo.setRecruiterName(recruiter.getUsername());
        }
        Resume resume = resumeMap.get(invitation.getSeekerId());
        if (resume != null && StringUtils.hasText(resume.getRealName())) {
            vo.setSeekerName(resume.getRealName());
        } else {
            SysUser seeker = userMap.get(invitation.getSeekerId());
            if (seeker != null) {
                vo.setSeekerName(seeker.getUsername());
            }
        }
        return vo;
    }
}
