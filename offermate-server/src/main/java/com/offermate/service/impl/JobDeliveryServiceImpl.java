package com.offermate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offermate.common.Result;
import com.offermate.constant.RedisConstants;
import com.offermate.dto.DeliveryCreateDTO;
import com.offermate.dto.DeliveryStatusDTO;
import com.offermate.dto.LoginUserDTO;
import com.offermate.entity.Company;
import com.offermate.entity.JobDelivery;
import com.offermate.entity.JobPosition;
import com.offermate.entity.Notification;
import com.offermate.entity.Resume;
import com.offermate.exception.BusinessException;
import com.offermate.mapper.JobDeliveryMapper;
import com.offermate.mapper.NotificationMapper;
import com.offermate.mq.RabbitMQProducer;
import com.offermate.mq.dto.DeliveryNoticeMessage;
import com.offermate.service.JobDeliveryService;
import com.offermate.service.JobPositionService;
import com.offermate.service.RedisCacheService;
import com.offermate.service.ResumeService;
import com.offermate.service.CompanyService;
import com.offermate.util.UserContext;
import com.offermate.vo.DeliveryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobDeliveryServiceImpl extends ServiceImpl<JobDeliveryMapper, JobDelivery> implements JobDeliveryService {

    private static final int JOB_SEEKER_ROLE = 1;
    private static final int RECRUITER_ROLE = 2;
    private static final int JOB_ONLINE_STATUS = 1;
    private static final int DELIVERED_STATUS = 1;
    private static final int UNSUITABLE_STATUS = 5;
    private static final int MIN_DELIVERY_STATUS = 1;
    private static final int MAX_DELIVERY_STATUS = 6;

    private final JobPositionService jobPositionService;
    private final CompanyService companyService;
    private final ResumeService resumeService;
    private final RedisCacheService redisCacheService;
    private final RabbitMQProducer rabbitMQProducer;
    private final NotificationMapper notificationMapper;

    @Override
    public void deliverJob(DeliveryCreateDTO dto) {
        LoginUserDTO loginUser = checkRole(JOB_SEEKER_ROLE);
        if (dto == null || dto.getJobId() == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        if (dto.getResumeId() == null) {
            throw new BusinessException("简历ID不能为空");
        }

        String lockKey = RedisConstants.DELIVERY_LOCK_KEY + loginUser.getUserId() + ":" + dto.getJobId();
        Boolean locked = redisCacheService.setIfAbsent(lockKey, "1", RedisConstants.DELIVERY_LOCK_TTL);
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException("请勿重复点击");
        }

        try {
            JobPosition job = jobPositionService.getById(dto.getJobId());
            if (job == null) {
                throw new BusinessException("岗位不存在");
            }
            if (job.getStatus() == null || job.getStatus() != JOB_ONLINE_STATUS) {
                throw new BusinessException("岗位已下架");
            }

            Resume resume = resumeService.getById(dto.getResumeId());
            if (resume == null) {
                throw new BusinessException("简历不存在");
            }
            if (!resume.getUserId().equals(loginUser.getUserId())) {
                throw new BusinessException("无权限操作该简历");
            }

            long count = count(new LambdaQueryWrapper<JobDelivery>()
                    .eq(JobDelivery::getUserId, loginUser.getUserId())
                    .eq(JobDelivery::getJobId, dto.getJobId()));
            if (count > 0) {
                throw new BusinessException("请勿重复投递");
            }

            JobDelivery delivery = new JobDelivery();
            delivery.setUserId(loginUser.getUserId());
            delivery.setJobId(job.getId());
            delivery.setCompanyId(job.getCompanyId());
            delivery.setResumeId(resume.getId());
            delivery.setStatus(DELIVERED_STATUS);
            save(delivery);
            sendDeliveryNotice(delivery, job, loginUser.getUserId());
        } catch (RuntimeException e) {
            if (Boolean.TRUE.equals(locked)) {
                redisCacheService.delete(lockKey);
            }
            throw e;
        }
    }

    private void sendDeliveryNotice(JobDelivery delivery, JobPosition job, Long seekerId) {
        DeliveryNoticeMessage message = new DeliveryNoticeMessage();
        message.setDeliveryId(delivery.getId());
        message.setSeekerId(seekerId);
        message.setRecruiterId(job.getRecruiterId());
        message.setJobId(job.getId());
        message.setJobTitle(job.getTitle());
        rabbitMQProducer.sendDeliveryNotice(message);
    }

    @Override
    public List<DeliveryVO> listMyDeliveries() {
        LoginUserDTO loginUser = checkRole(JOB_SEEKER_ROLE);
        List<JobDelivery> deliveries = list(new LambdaQueryWrapper<JobDelivery>()
                .eq(JobDelivery::getUserId, loginUser.getUserId())
                .orderByDesc(JobDelivery::getCreateTime));
        return buildDeliveryVOList(deliveries);
    }

    @Override
    public List<DeliveryVO> listCompanyDeliveries() {
        LoginUserDTO loginUser = checkRole(RECRUITER_ROLE);
        List<Long> jobIds = jobPositionService.list(new LambdaQueryWrapper<JobPosition>()
                        .eq(JobPosition::getRecruiterId, loginUser.getUserId()))
                .stream()
                .map(JobPosition::getId)
                .toList();
        if (jobIds.isEmpty()) {
            return List.of();
        }

        List<JobDelivery> deliveries = list(new LambdaQueryWrapper<JobDelivery>()
                .in(JobDelivery::getJobId, jobIds)
                .orderByDesc(JobDelivery::getCreateTime));
        return buildDeliveryVOList(deliveries);
    }

    @Override
    public void updateDeliveryStatus(Long id, DeliveryStatusDTO dto) {
        LoginUserDTO loginUser = checkRole(RECRUITER_ROLE);
        if (dto == null || dto.getStatus() == null || dto.getStatus() < MIN_DELIVERY_STATUS || dto.getStatus() > MAX_DELIVERY_STATUS) {
            throw new BusinessException("投递状态不合法");
        }

        JobDelivery delivery = getById(id);
        if (delivery == null) {
            throw new BusinessException("投递记录不存在");
        }

        JobPosition job = jobPositionService.getById(delivery.getJobId());
        if (job == null || !job.getRecruiterId().equals(loginUser.getUserId())) {
            throw new BusinessException("无权限操作该投递");
        }

        Integer oldStatus = delivery.getStatus();
        Integer newStatus = dto.getStatus();
        delivery.setStatus(newStatus);
        updateById(delivery);

        if (!newStatus.equals(oldStatus)) {
            createStatusChangeNotification(delivery, job, newStatus);
        }
    }

    @Override
    public void deleteMyDelivery(Long id) {
        LoginUserDTO loginUser = checkRole(JOB_SEEKER_ROLE);
        JobDelivery delivery = getById(id);
        if (delivery == null) {
            throw new BusinessException("投递记录不存在");
        }
        if (!delivery.getUserId().equals(loginUser.getUserId())) {
            throw new BusinessException("无权限删除该投递记录");
        }
        if (!Integer.valueOf(UNSUITABLE_STATUS).equals(delivery.getStatus())) {
            throw new BusinessException("仅不合适状态的投递记录可删除");
        }
        removeById(id);
    }

    private void createStatusChangeNotification(JobDelivery delivery, JobPosition job, Integer status) {
        NoticeContent noticeContent = buildNoticeContent(job.getTitle(), status);
        if (noticeContent == null) {
            return;
        }

        try {
            long exists = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                    .eq(Notification::getUserId, delivery.getUserId())
                    .eq(Notification::getType, noticeContent.type())
                    .eq(Notification::getTitle, noticeContent.title())
                    .eq(Notification::getContent, noticeContent.content()));
            if (exists > 0) {
                return;
            }

            Notification notification = new Notification();
            notification.setUserId(delivery.getUserId());
            notification.setTitle(noticeContent.title());
            notification.setContent(noticeContent.content());
            notification.setType(noticeContent.type());
            notification.setIsRead(0);
            notification.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(notification);
        } catch (Exception e) {
            log.warn("投递状态通知写入失败 deliveryId={}, status={}", delivery.getId(), status, e);
        }
    }

    private NoticeContent buildNoticeContent(String jobTitle, Integer status) {
        String title = jobTitle == null || jobTitle.isBlank() ? "该岗位" : jobTitle;
        return switch (status) {
            case 3 -> new NoticeContent("企业对你感兴趣", "你投递的【" + title + "】企业表示感兴趣，请保持联系畅通。", 4);
            case 4 -> new NoticeContent("面试邀请", "你投递的【" + title + "】已收到面试邀请，请及时查看。", 4);
            case 5 -> new NoticeContent("投递进度更新", "你投递的【" + title + "】暂时不太匹配，继续加油。", 5);
            case 6 -> new NoticeContent("录用通知", "恭喜！你投递的【" + title + "】已进入录用状态。", 4);
            default -> null;
        };
    }

    private record NoticeContent(String title, String content, Integer type) {
    }

    private LoginUserDTO checkRole(int role) {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getRole() == null || loginUser.getRole() != role) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }
        return loginUser;
    }

    private List<DeliveryVO> buildDeliveryVOList(List<JobDelivery> deliveries) {
        if (deliveries.isEmpty()) {
            return List.of();
        }

        List<Long> jobIds = deliveries.stream()
                .map(JobDelivery::getJobId)
                .distinct()
                .toList();
        Map<Long, JobPosition> jobMap = jobPositionService.listByIds(jobIds)
                .stream()
                .collect(Collectors.toMap(JobPosition::getId, Function.identity(), (a, b) -> a));
        List<Long> resumeIds = deliveries.stream()
                .map(JobDelivery::getResumeId)
                .distinct()
                .toList();
        Map<Long, Resume> resumeMap = resumeService.listByIds(resumeIds)
                .stream()
                .collect(Collectors.toMap(Resume::getId, Function.identity(), (a, b) -> a));

        List<Long> companyIds = jobMap.values().stream()
                .map(JobPosition::getCompanyId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, Company> companyMap = companyIds.isEmpty() ? Map.of() : companyService.listByIds(companyIds)
                .stream()
                .collect(Collectors.toMap(Company::getId, Function.identity(), (a, b) -> a));

        return deliveries.stream()
                .map(delivery -> toVO(delivery, jobMap.get(delivery.getJobId()),
                        resumeMap.get(delivery.getResumeId()), companyMap))
                .toList();
    }

    private DeliveryVO toVO(JobDelivery delivery, JobPosition job, Resume resume, Map<Long, Company> companyMap) {
        DeliveryVO vo = new DeliveryVO();
        vo.setId(delivery.getId());
        vo.setUserId(delivery.getUserId());
        vo.setJobId(delivery.getJobId());
        vo.setCompanyId(delivery.getCompanyId());
        vo.setResumeId(delivery.getResumeId());
        vo.setStatus(delivery.getStatus());
        vo.setCreateTime(delivery.getCreateTime());
        if (resume != null) {
            vo.setResumeTitle(resume.getTitle());
        }
        if (job != null) {
            vo.setJobTitle(job.getTitle());
            vo.setTitle(job.getTitle());
            vo.setRecruiterId(job.getRecruiterId());
            vo.setCity(job.getCity());
            vo.setSalaryMin(job.getSalaryMin());
            vo.setSalaryMax(job.getSalaryMax());
            Company company = companyMap.get(job.getCompanyId());
            if (company != null) {
                vo.setCompanyName(company.getCompanyName());
            }
        }
        return vo;
    }
}
