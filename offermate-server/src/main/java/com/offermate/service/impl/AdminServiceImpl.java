package com.offermate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.offermate.common.Result;
import com.offermate.common.result.PageResult;
import com.offermate.dto.AdminCompanyAuditPageQueryDTO;
import com.offermate.dto.AdminJobAuditPageQueryDTO;
import com.offermate.dto.AdminUserPageQueryDTO;
import com.offermate.dto.AuditDTO;
import com.offermate.dto.LoginUserDTO;
import com.offermate.entity.Company;
import com.offermate.entity.JobPosition;
import com.offermate.entity.SysUser;
import com.offermate.exception.BusinessException;
import com.offermate.mq.RabbitMQProducer;
import com.offermate.mq.dto.AuditNoticeMessage;
import com.offermate.service.AdminService;
import com.offermate.service.CompanyService;
import com.offermate.service.JobEsService;
import com.offermate.service.JobPositionService;
import com.offermate.service.SysUserService;
import com.offermate.util.PageUtils;
import com.offermate.util.UserContext;
import com.offermate.vo.AdminCompanyAuditVO;
import com.offermate.vo.AdminJobAuditVO;
import com.offermate.vo.AdminUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final int ADMIN_ROLE = 3;
    private static final int NORMAL_STATUS = 1;
    private static final int DISABLED_STATUS = 0;
    private static final int AUDIT_PASS = 1;
    private static final int AUDIT_REJECT = 2;

    private final SysUserService sysUserService;
    private final CompanyService companyService;
    private final JobPositionService jobPositionService;
    private final JobEsService jobEsService;
    private final RabbitMQProducer rabbitMQProducer;

    @Override
    public PageResult<AdminUserVO> pageUsers(AdminUserPageQueryDTO dto) {
        checkAdmin();
        if (dto == null) {
            dto = new AdminUserPageQueryDTO();
        }
        AdminUserPageQueryDTO query = dto;
        Page<SysUser> pageInfo = sysUserService.page(new Page<>(PageUtils.page(query.getPage()), PageUtils.pageSize(query.getPageSize())),
                new LambdaQueryWrapper<SysUser>()
                        .and(StringUtils.hasText(query.getKeyword()), w -> w
                                .like(SysUser::getUsername, query.getKeyword())
                                .or()
                                .like(SysUser::getPhone, query.getKeyword()))
                        .eq(query.getRole() != null, SysUser::getRole, query.getRole())
                        .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                        .orderByDesc(SysUser::getCreateTime));
        List<AdminUserVO> records = pageInfo.getRecords().stream().map(this::toUserVO).toList();
        return new PageResult<>(pageInfo.getTotal(), records);
    }

    @Override
    public void disableUser(Long id) {
        LoginUserDTO admin = checkAdmin();
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (admin.getUserId().equals(id)) {
            throw new BusinessException("不能禁用当前登录管理员");
        }
        user.setStatus(DISABLED_STATUS);
        sysUserService.updateById(user);
    }

    @Override
    public void enableUser(Long id) {
        checkAdmin();
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(NORMAL_STATUS);
        sysUserService.updateById(user);
    }

    @Override
    public PageResult<AdminCompanyAuditVO> pageCompanyAudits(AdminCompanyAuditPageQueryDTO dto) {
        checkAdmin();
        if (dto == null) {
            dto = new AdminCompanyAuditPageQueryDTO();
        }
        AdminCompanyAuditPageQueryDTO query = dto;
        Page<Company> pageInfo = companyService.page(new Page<>(PageUtils.page(query.getPage()), PageUtils.pageSize(query.getPageSize())),
                new LambdaQueryWrapper<Company>()
                        .like(StringUtils.hasText(query.getKeyword()), Company::getCompanyName, query.getKeyword())
                        .eq(query.getAuditStatus() != null, Company::getAuditStatus, query.getAuditStatus())
                        .orderByDesc(Company::getUpdateTime));
        List<AdminCompanyAuditVO> records = pageInfo.getRecords().stream().map(this::toCompanyAuditVO).toList();
        return new PageResult<>(pageInfo.getTotal(), records);
    }

    @Override
    public void auditCompany(Long id, AuditDTO dto) {
        checkAdmin();
        checkAuditStatus(dto);
        Company company = companyService.getById(id);
        if (company == null) {
            throw new BusinessException("企业不存在");
        }
        company.setAuditStatus(dto.getAuditStatus());
        companyService.updateById(company);
        sendCompanyAuditNotice(company, dto.getAuditStatus());
    }

    @Override
    public PageResult<AdminJobAuditVO> pageJobAudits(AdminJobAuditPageQueryDTO dto) {
        checkAdmin();
        if (dto == null) {
            dto = new AdminJobAuditPageQueryDTO();
        }
        AdminJobAuditPageQueryDTO query = dto;
        Page<JobPosition> pageInfo = jobPositionService.page(new Page<>(PageUtils.page(query.getPage()), PageUtils.pageSize(query.getPageSize())),
                new LambdaQueryWrapper<JobPosition>()
                        .like(StringUtils.hasText(query.getKeyword()), JobPosition::getTitle, query.getKeyword())
                        .eq(query.getAuditStatus() != null, JobPosition::getAuditStatus, query.getAuditStatus())
                        .orderByDesc(JobPosition::getCreateTime));
        List<AdminJobAuditVO> records = buildJobAuditVOList(pageInfo.getRecords());
        return new PageResult<>(pageInfo.getTotal(), records);
    }

    @Override
    public void auditJob(Long id, AuditDTO dto) {
        checkAdmin();
        checkAuditStatus(dto);
        JobPosition job = jobPositionService.getById(id);
        if (job == null) {
            throw new BusinessException("岗位不存在");
        }
        job.setAuditStatus(dto.getAuditStatus());
        jobPositionService.updateById(job);
        syncAuditJobToEs(job.getId(), dto.getAuditStatus());
        sendJobAuditNotice(job, dto.getAuditStatus());
    }

    private void syncAuditJobToEs(Long jobId, Integer auditStatus) {
        try {
            if (auditStatus == AUDIT_PASS) {
                jobEsService.saveOrUpdateJob(jobId);
            } else {
                jobEsService.deleteJob(jobId);
            }
        } catch (Exception e) {
            log.warn("ES岗位审核同步失败 jobId={}", jobId, e);
        }
    }

    private LoginUserDTO checkAdmin() {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getRole() == null || loginUser.getRole() != ADMIN_ROLE) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }
        return loginUser;
    }

    private void checkAuditStatus(AuditDTO dto) {
        if (dto == null || (dto.getAuditStatus() != AUDIT_PASS && dto.getAuditStatus() != AUDIT_REJECT)) {
            throw new BusinessException("审核状态错误");
        }
    }

    private void sendCompanyAuditNotice(Company company, Integer auditStatus) {
        AuditNoticeMessage message = new AuditNoticeMessage();
        message.setUserId(company.getUserId());
        message.setTargetName(company.getCompanyName());
        message.setAuditStatus(auditStatus);
        message.setAuditType("company");
        rabbitMQProducer.sendCompanyAuditNotice(message);
    }

    private void sendJobAuditNotice(JobPosition job, Integer auditStatus) {
        AuditNoticeMessage message = new AuditNoticeMessage();
        message.setUserId(job.getRecruiterId());
        message.setTargetName(job.getTitle());
        message.setAuditStatus(auditStatus);
        message.setAuditType("job");
        rabbitMQProducer.sendJobAuditNotice(message);
    }

    private AdminUserVO toUserVO(SysUser user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }

    private AdminCompanyAuditVO toCompanyAuditVO(Company company) {
        AdminCompanyAuditVO vo = new AdminCompanyAuditVO();
        vo.setId(company.getId());
        vo.setUserId(company.getUserId());
        SysUser user = sysUserService.getById(company.getUserId());
        vo.setUsername(user == null ? null : user.getUsername());
        vo.setCompanyName(company.getCompanyName());
        vo.setLogo(company.getLogo());
        vo.setIndustry(company.getIndustry());
        vo.setScale(company.getScale());
        vo.setAddress(company.getAddress());
        vo.setIntro(company.getIntro());
        vo.setAuditStatus(company.getAuditStatus());
        vo.setCreateTime(company.getCreateTime());
        vo.setUpdateTime(company.getUpdateTime());
        return vo;
    }

    private List<AdminJobAuditVO> buildJobAuditVOList(List<JobPosition> jobs) {
        if (jobs.isEmpty()) {
            return List.of();
        }
        List<Long> companyIds = jobs.stream().map(JobPosition::getCompanyId).distinct().toList();
        List<Long> recruiterIds = jobs.stream().map(JobPosition::getRecruiterId).distinct().toList();
        Map<Long, Company> companyMap = companyService.listByIds(companyIds).stream()
                .collect(Collectors.toMap(Company::getId, Function.identity(), (a, b) -> a));
        Map<Long, SysUser> userMap = sysUserService.listByIds(recruiterIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
        return jobs.stream().map(job -> toJobAuditVO(job, companyMap, userMap)).toList();
    }

    private AdminJobAuditVO toJobAuditVO(JobPosition job, Map<Long, Company> companyMap, Map<Long, SysUser> userMap) {
        AdminJobAuditVO vo = new AdminJobAuditVO();
        vo.setId(job.getId());
        vo.setCompanyId(job.getCompanyId());
        vo.setRecruiterId(job.getRecruiterId());
        vo.setTitle(job.getTitle());
        vo.setSalaryMin(job.getSalaryMin());
        vo.setSalaryMax(job.getSalaryMax());
        vo.setCity(job.getCity());
        vo.setExperience(job.getExperience());
        vo.setEducation(job.getEducation());
        vo.setTags(job.getTags());
        vo.setDescription(job.getDescription());
        vo.setStatus(job.getStatus());
        vo.setAuditStatus(job.getAuditStatus());
        vo.setViewCount(job.getViewCount());
        Company company = companyMap.get(job.getCompanyId());
        SysUser recruiter = userMap.get(job.getRecruiterId());
        vo.setCompanyName(company == null ? null : company.getCompanyName());
        vo.setRecruiterUsername(recruiter == null ? null : recruiter.getUsername());
        vo.setCreateTime(job.getCreateTime());
        vo.setUpdateTime(job.getUpdateTime());
        return vo;
    }
}
