package com.offermate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offermate.common.Result;
import com.offermate.dto.CompanySaveDTO;
import com.offermate.dto.LoginUserDTO;
import com.offermate.entity.Company;
import com.offermate.entity.JobPosition;
import com.offermate.exception.BusinessException;
import com.offermate.mapper.CompanyMapper;
import com.offermate.mapper.JobPositionMapper;
import com.offermate.service.CompanyService;
import com.offermate.util.UserContext;
import com.offermate.vo.CompanyVO;
import com.offermate.vo.JobPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company> implements CompanyService {

    private static final int RECRUITER_ROLE = 2;
    private static final int WAIT_AUDIT_STATUS = 0;
    private static final int ONLINE_STATUS = 1;
    private static final int PUBLIC_JOB_LIMIT = 20;

    private final JobPositionMapper jobPositionMapper;

    @Override
    public CompanyVO getMyCompany() {
        LoginUserDTO loginUser = checkRecruiter();
        Company company = getByUserId(loginUser.getUserId());
        return company == null ? null : toVO(company);
    }

    @Override
    public void saveMyCompany(CompanySaveDTO dto) {
        LoginUserDTO loginUser = checkRecruiter();
        if (dto == null || !StringUtils.hasText(dto.getCompanyName())) {
            throw new BusinessException("企业名称不能为空");
        }

        Company company = getByUserId(loginUser.getUserId());
        if (company == null) {
            company = new Company();
            company.setUserId(loginUser.getUserId());
            company.setAuditStatus(WAIT_AUDIT_STATUS);
        }

        company.setCompanyName(trim(dto.getCompanyName()));
        company.setLogo(trim(dto.getLogo()));
        company.setIndustry(trim(dto.getIndustry()));
        company.setScale(trim(dto.getScale()));
        company.setAddress(trim(dto.getAddress()));
        company.setIntro(trim(dto.getIntro()));

        if (company.getId() == null) {
            save(company);
        } else {
            updateById(company);
        }
    }

    @Override
    public CompanyVO getCompanyDetail(Long id) {
        Company company = getById(id);
        if (company == null) {
            throw new BusinessException("企业不存在");
        }
        CompanyVO vo = toVO(company);
        vo.setJobs(listPublicCompanyJobs(company));
        return vo;
    }

    @Override
    public Company getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<Company>()
                .eq(Company::getUserId, userId), false);
    }

    @Override
    public void updateLogo(String logo) {
        LoginUserDTO loginUser = checkRecruiter();
        if (!StringUtils.hasText(logo)) {
            throw new BusinessException("企业Logo地址不能为空");
        }
        Company company = getByUserId(loginUser.getUserId());
        if (company == null) {
            throw new BusinessException("请先完善企业信息");
        }
        company.setLogo(logo.trim());
        updateById(company);
    }

    private LoginUserDTO checkRecruiter() {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getRole() == null || loginUser.getRole() != RECRUITER_ROLE) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }
        return loginUser;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private CompanyVO toVO(Company company) {
        CompanyVO vo = new CompanyVO();
        vo.setId(company.getId());
        vo.setUserId(company.getUserId());
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

    private List<JobPageVO> listPublicCompanyJobs(Company company) {
        return jobPositionMapper.selectList(new LambdaQueryWrapper<JobPosition>()
                        .eq(JobPosition::getCompanyId, company.getId())
                        .eq(JobPosition::getStatus, ONLINE_STATUS)
                        .orderByDesc(JobPosition::getCreateTime)
                        .last("limit " + PUBLIC_JOB_LIMIT))
                .stream()
                .map(job -> toJobPageVO(job, company))
                .toList();
    }

    private JobPageVO toJobPageVO(JobPosition job, Company company) {
        JobPageVO vo = new JobPageVO();
        vo.setId(job.getId());
        vo.setCompanyId(job.getCompanyId());
        vo.setTitle(job.getTitle());
        vo.setSalaryMin(job.getSalaryMin());
        vo.setSalaryMax(job.getSalaryMax());
        vo.setCity(job.getCity());
        vo.setExperience(job.getExperience());
        vo.setEducation(job.getEducation());
        vo.setTags(job.getTags());
        vo.setStatus(job.getStatus());
        vo.setViewCount(job.getViewCount());
        vo.setCreateTime(job.getCreateTime());
        vo.setCompanyName(company.getCompanyName());
        vo.setCompanyLogo(company.getLogo());
        vo.setIndustry(company.getIndustry());
        vo.setScale(company.getScale());
        return vo;
    }
}
