package com.offermate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offermate.common.Result;
import com.offermate.constant.RedisConstants;
import com.offermate.dto.ChatSessionCreateDTO;
import com.offermate.dto.LoginUserDTO;
import com.offermate.entity.ChatMessage;
import com.offermate.entity.ChatSession;
import com.offermate.entity.Company;
import com.offermate.entity.JobPosition;
import com.offermate.entity.Resume;
import com.offermate.entity.SysUser;
import com.offermate.exception.BusinessException;
import com.offermate.mapper.ChatMessageMapper;
import com.offermate.mapper.ChatSessionMapper;
import com.offermate.service.ChatSessionService;
import com.offermate.service.CompanyService;
import com.offermate.service.JobPositionService;
import com.offermate.service.RedisCacheService;
import com.offermate.service.ResumeService;
import com.offermate.service.SysUserService;
import com.offermate.util.UserContext;
import com.offermate.vo.ChatSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    private static final int SEEKER_ROLE = 1;
    private static final int RECRUITER_ROLE = 2;

    private final ChatMessageMapper chatMessageMapper;
    private final SysUserService sysUserService;
    private final CompanyService companyService;
    private final JobPositionService jobPositionService;
    private final ResumeService resumeService;
    private final RedisCacheService redisCacheService;

    @Override
    public ChatSessionVO createOrGetSession(ChatSessionCreateDTO dto) {
        LoginUserDTO loginUser = getChatUser();
        if (dto == null || dto.getJobId() == null) {
            throw new BusinessException("岗位ID不能为空");
        }

        JobPosition job = jobPositionService.getById(dto.getJobId());
        if (job == null) {
            throw new BusinessException("岗位不存在");
        }

        Long targetUserId = dto.getTargetUserId() != null ? dto.getTargetUserId() : dto.getRecruiterId();
        if (targetUserId == null && loginUser.getRole() == SEEKER_ROLE) {
            targetUserId = job.getRecruiterId();
        }
        if (targetUserId == null) {
            throw new BusinessException("目标用户ID不能为空");
        }

        Long seekerId;
        Long recruiterId;
        if (loginUser.getRole() == SEEKER_ROLE) {
            seekerId = loginUser.getUserId();
            recruiterId = targetUserId;
        } else {
            recruiterId = loginUser.getUserId();
            seekerId = targetUserId;
        }
        if (!job.getRecruiterId().equals(recruiterId)) {
            throw new BusinessException("无权限操作该会话");
        }

        ChatSession session = getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSeekerId, seekerId)
                .eq(ChatSession::getRecruiterId, recruiterId)
                .eq(ChatSession::getJobId, dto.getJobId()), false);
        if (session == null) {
            session = new ChatSession();
            session.setSeekerId(seekerId);
            session.setRecruiterId(recruiterId);
            session.setJobId(dto.getJobId());
            session.setUpdateTime(LocalDateTime.now());
            save(session);
        }
        return toVO(session, loginUser);
    }

    @Override
    public List<ChatSessionVO> listMySessions() {
        LoginUserDTO loginUser = getChatUser();
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<ChatSession>()
                .eq(loginUser.getRole() == SEEKER_ROLE, ChatSession::getSeekerId, loginUser.getUserId())
                .eq(loginUser.getRole() == RECRUITER_ROLE, ChatSession::getRecruiterId, loginUser.getUserId())
                .orderByDesc(ChatSession::getUpdateTime);
        return list(wrapper).stream()
                .map(session -> toVO(session, loginUser))
                .toList();
    }

    @Override
    public ChatSession checkSessionParticipant(Long sessionId, Long userId) {
        ChatSession session = getById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        if (!session.getSeekerId().equals(userId) && !session.getRecruiterId().equals(userId)) {
            throw new BusinessException("无权限操作该会话");
        }
        return session;
    }

    private LoginUserDTO getChatUser() {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getRole() == null
                || (loginUser.getRole() != SEEKER_ROLE && loginUser.getRole() != RECRUITER_ROLE)) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }
        return loginUser;
    }

    private ChatSessionVO toVO(ChatSession session, LoginUserDTO loginUser) {
        ChatSessionVO vo = new ChatSessionVO();
        vo.setId(session.getId());
        vo.setSessionId(session.getId());
        vo.setSeekerId(session.getSeekerId());
        vo.setRecruiterId(session.getRecruiterId());
        vo.setJobId(session.getJobId());
        vo.setLastMessage(session.getLastMessage());
        vo.setUpdateTime(session.getUpdateTime());
        vo.setUnreadCount(getUnreadCount(loginUser.getUserId(), session.getId()));
        fillDisplayInfo(vo, session, loginUser);
        return vo;
    }

    private Integer getUnreadCount(Long userId, Long sessionId) {
        String key = RedisConstants.CHAT_UNREAD_KEY + userId + ":" + sessionId;
        String value = redisCacheService.get(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                redisCacheService.delete(key);
            }
        }
        Long unreadCount = chatMessageMapper.selectCount(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getIsRead, 0));
        redisCacheService.set(key, String.valueOf(unreadCount), RedisConstants.CHAT_UNREAD_TTL);
        return unreadCount.intValue();
    }

    private void fillDisplayInfo(ChatSessionVO vo, ChatSession session, LoginUserDTO loginUser) {
        Long oppositeUserId = loginUser.getRole() == SEEKER_ROLE ? session.getRecruiterId() : session.getSeekerId();
        vo.setOppositeUserId(oppositeUserId);

        SysUser oppositeUser = sysUserService.getById(oppositeUserId);
        if (oppositeUser != null) {
            vo.setOppositeName(oppositeUser.getUsername());
            vo.setOppositeAvatar(oppositeUser.getAvatar());
        }

        JobPosition job = jobPositionService.getById(session.getJobId());
        if (job != null) {
            vo.setJobTitle(job.getTitle());
        }

        Company recruiterCompany = companyService.getByUserId(session.getRecruiterId());
        if (recruiterCompany != null) {
            vo.setCompanyName(recruiterCompany.getCompanyName());
            vo.setCompanyLogo(recruiterCompany.getLogo());
        }

        String seekerName = getSeekerName(session.getSeekerId());
        vo.setSeekerName(seekerName);

        if (loginUser.getRole() == SEEKER_ROLE && recruiterCompany != null) {
            vo.setOppositeName(recruiterCompany.getCompanyName());
            vo.setOppositeAvatar(recruiterCompany.getLogo());
        }
        if (loginUser.getRole() == RECRUITER_ROLE && seekerName != null) {
            vo.setOppositeName(seekerName);
        }
    }

    private String getSeekerName(Long seekerId) {
        Resume resume = resumeService.getOne(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, seekerId)
                .orderByDesc(Resume::getIsDefault)
                .orderByDesc(Resume::getCreateTime)
                .last("LIMIT 1"), false);
        if (resume != null && resume.getRealName() != null && !resume.getRealName().isBlank()) {
            return resume.getRealName();
        }
        SysUser seeker = sysUserService.getById(seekerId);
        return seeker == null ? null : seeker.getUsername();
    }
}
