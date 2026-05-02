package com.offermate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offermate.common.Result;
import com.offermate.common.result.PageResult;
import com.offermate.constant.RedisConstants;
import com.offermate.dto.JobCreateDTO;
import com.offermate.dto.JobPageQueryDTO;
import com.offermate.dto.JobSearchDTO;
import com.offermate.dto.JobUpdateDTO;
import com.offermate.dto.LoginUserDTO;
import com.offermate.entity.Company;
import com.offermate.entity.JobPosition;
import com.offermate.exception.BusinessException;
import com.offermate.mapper.JobPositionMapper;
import com.offermate.service.CompanyService;
import com.offermate.service.JobEsService;
import com.offermate.service.JobPositionService;
import com.offermate.service.RedisCacheService;
import com.offermate.util.PageUtils;
import com.offermate.util.UserContext;
import com.offermate.vo.JobDetailVO;
import com.offermate.vo.JobPageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPositionServiceImpl extends ServiceImpl<JobPositionMapper, JobPosition> implements JobPositionService {

    private static final int RECRUITER_ROLE = 2;
    private static final int ADMIN_ROLE = 3;
    private static final int ONLINE_STATUS = 1;
    private static final int OFFLINE_STATUS = 0;

    private final CompanyService companyService;
    private final JobEsService jobEsService;
    private final RedisCacheService redisCacheService;
    private final ObjectMapper objectMapper;

    @Override
    public void createJob(JobCreateDTO dto) {
        LoginUserDTO loginUser = checkRecruiter();
        if (dto == null || !StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException("岗位名称不能为空");
        }
        checkSalary(dto.getSalaryMin(), dto.getSalaryMax());
        Company company = companyService.getByUserId(loginUser.getUserId());
        if (company == null) {
            throw new BusinessException("请先完善企业信息");
        }

        JobPosition job = new JobPosition();
        job.setCompanyId(company.getId());
        job.setRecruiterId(loginUser.getUserId());
        job.setTitle(dto.getTitle());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        job.setCity(dto.getCity());
        job.setExperience(dto.getExperience());
        job.setEducation(dto.getEducation());
        job.setTags(dto.getTags());
        job.setDescription(dto.getDescription());
        job.setStatus(ONLINE_STATUS);
        job.setViewCount(0);
        save(job);
        clearJobPageCache();
        syncJobToEs(job.getId());
    }

    @Override
    public void updateJob(Long id, JobUpdateDTO dto) {
        LoginUserDTO loginUser = checkRecruiter();
        JobPosition job = checkJobOwner(id, loginUser.getUserId());
        if (dto == null) {
            return;
        }
        checkSalary(dto.getSalaryMin(), dto.getSalaryMax());

        job.setTitle(dto.getTitle());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        job.setCity(dto.getCity());
        job.setExperience(dto.getExperience());
        job.setEducation(dto.getEducation());
        job.setTags(dto.getTags());
        job.setDescription(dto.getDescription());
        updateById(job);
        clearJobCache(id);
        syncJobToEs(id);
    }

    @Override
    public void deleteJob(Long id) {
        LoginUserDTO loginUser = checkRecruiter();
        checkJobOwner(id, loginUser.getUserId());
        removeById(id);
        clearJobCache(id);
        deleteJobFromEs(id);
    }

    @Override
    public void offlineJob(Long id) {
        LoginUserDTO loginUser = checkRecruiter();
        JobPosition job = checkJobOwner(id, loginUser.getUserId());
        job.setStatus(OFFLINE_STATUS);
        updateById(job);
        clearJobCache(id);
        deleteJobFromEs(id);
    }

    @Override
    public JobDetailVO getJobDetail(Long id) {
        String cacheKey = RedisConstants.JOB_DETAIL_KEY + id;
        JobDetailVO cached = getCache(cacheKey, JobDetailVO.class);
        if (cached != null) {
            log.debug("岗位详情缓存命中 key={}", cacheKey);
            return cached;
        }

        JobPosition job = getById(id);
        if (job == null) {
            throw new BusinessException("岗位不存在");
        }

        baseMapper.update(null, new UpdateWrapper<JobPosition>()
                .eq("id", id)
                .setSql("view_count = view_count + 1"));
        job = getById(id);
        JobDetailVO vo = toDetailVO(job);
        setCache(cacheKey, vo, RedisConstants.JOB_DETAIL_TTL);
        return vo;
    }

    @Override
    public PageResult<JobPageVO> pageJobs(JobPageQueryDTO dto) {
        if (dto == null) {
            dto = new JobPageQueryDTO();
        }
        JobPageQueryDTO queryDTO = dto;
        int page = PageUtils.page(queryDTO.getPage());
        int pageSize = PageUtils.pageSize(queryDTO.getPageSize());
        String cacheKey = buildJobPageCacheKey(queryDTO, page, pageSize);
        PageResult<JobPageVO> cached = getCache(cacheKey, new TypeReference<PageResult<JobPageVO>>() {
        });
        if (cached != null) {
            log.debug("岗位分页缓存命中 key={}", cacheKey);
            return cached;
        }

        LambdaQueryWrapper<JobPosition> wrapper = new LambdaQueryWrapper<JobPosition>()
                .eq(JobPosition::getStatus, ONLINE_STATUS)
                .and(StringUtils.hasText(queryDTO.getKeyword()), w -> w
                        .like(JobPosition::getTitle, queryDTO.getKeyword())
                        .or()
                        .like(JobPosition::getDescription, queryDTO.getKeyword()))
                .eq(StringUtils.hasText(queryDTO.getCity()), JobPosition::getCity, queryDTO.getCity())
                .eq(StringUtils.hasText(queryDTO.getEducation()), JobPosition::getEducation, queryDTO.getEducation())
                .eq(StringUtils.hasText(queryDTO.getExperience()), JobPosition::getExperience, queryDTO.getExperience())
                .ge(queryDTO.getSalaryMin() != null, JobPosition::getSalaryMax, queryDTO.getSalaryMin())
                .le(queryDTO.getSalaryMax() != null, JobPosition::getSalaryMin, queryDTO.getSalaryMax())
                .orderByDesc(JobPosition::getCreateTime);

        Page<JobPosition> pageInfo = page(new Page<>(page, pageSize), wrapper);
        List<JobPageVO> records = pageInfo.getRecords().stream().map(this::toPageVO).toList();
        PageResult<JobPageVO> result = new PageResult<>(pageInfo.getTotal(), records);
        setCache(cacheKey, result, RedisConstants.JOB_PAGE_TTL);
        return result;
    }

    @Override
    public PageResult<JobPageVO> searchJobs(JobSearchDTO dto) {
        return jobEsService.searchJobs(dto);
    }

    @Override
    public int syncJobsToEs() {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getRole() == null || loginUser.getRole() != ADMIN_ROLE) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }
        return jobEsService.syncAll();
    }

    @Override
    public List<JobPageVO> listCompanyJobs() {
        LoginUserDTO loginUser = checkRecruiter();
        return list(new LambdaQueryWrapper<JobPosition>()
                .eq(JobPosition::getRecruiterId, loginUser.getUserId())
                .orderByDesc(JobPosition::getCreateTime))
                .stream()
                .map(this::toPageVO)
                .toList();
    }

    private LoginUserDTO checkRecruiter() {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getRole() == null || loginUser.getRole() != RECRUITER_ROLE) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }
        return loginUser;
    }

    private JobPosition checkJobOwner(Long id, Long userId) {
        JobPosition job = getById(id);
        if (job == null) {
            throw new BusinessException("岗位不存在");
        }
        if (!job.getRecruiterId().equals(userId)) {
            throw new BusinessException("无权限操作该岗位");
        }
        return job;
    }

    private void checkSalary(Integer salaryMin, Integer salaryMax) {
        if (salaryMin != null && salaryMax != null && salaryMin > salaryMax) {
            throw new BusinessException("最低薪资不能大于最高薪资");
        }
    }

    private void clearJobCache(Long id) {
        redisCacheService.delete(RedisConstants.JOB_DETAIL_KEY + id);
        clearJobPageCache();
    }

    private void clearJobPageCache() {
        redisCacheService.deleteByPrefix(RedisConstants.JOB_PAGE_KEY);
    }

    private String buildJobPageCacheKey(JobPageQueryDTO dto, int page, int pageSize) {
        String source = "keyword=" + nullToEmpty(dto.getKeyword())
                + "&city=" + nullToEmpty(dto.getCity())
                + "&salaryMin=" + nullToEmpty(dto.getSalaryMin())
                + "&salaryMax=" + nullToEmpty(dto.getSalaryMax())
                + "&education=" + nullToEmpty(dto.getEducation())
                + "&experience=" + nullToEmpty(dto.getExperience())
                + "&page=" + page
                + "&pageSize=" + pageSize;
        return RedisConstants.JOB_PAGE_KEY + md5(source);
    }

    private String nullToEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String md5(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            return String.valueOf(source.hashCode());
        }
    }

    private <T> T getCache(String key, Class<T> clazz) {
        String value = redisCacheService.get(key);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return objectMapper.readValue(value, clazz);
        } catch (Exception e) {
            log.warn("岗位缓存反序列化失败 key={}", key, e);
            redisCacheService.delete(key);
            return null;
        }
    }

    private <T> T getCache(String key, TypeReference<T> typeReference) {
        String value = redisCacheService.get(key);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (Exception e) {
            log.warn("岗位缓存反序列化失败 key={}", key, e);
            redisCacheService.delete(key);
            return null;
        }
    }

    private void setCache(String key, Object value, Duration ttl) {
        try {
            redisCacheService.set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception e) {
            log.warn("岗位缓存序列化失败 key={}", key, e);
        }
    }

    private JobDetailVO toDetailVO(JobPosition job) {
        JobDetailVO vo = new JobDetailVO();
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
        vo.setCreateTime(job.getCreateTime());
        vo.setUpdateTime(job.getUpdateTime());
        Company company = companyService.getById(job.getCompanyId());
        fillCompanyInfo(vo, company);
        return vo;
    }

    private JobPageVO toPageVO(JobPosition job) {
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
        vo.setAuditStatus(job.getAuditStatus());
        vo.setViewCount(job.getViewCount());
        vo.setCreateTime(job.getCreateTime());
        fillCompanyInfo(vo, companyService.getById(job.getCompanyId()));
        return vo;
    }

    private void fillCompanyInfo(JobPageVO vo, Company company) {
        if (company == null) {
            return;
        }
        vo.setCompanyName(company.getCompanyName());
        vo.setCompanyLogo(company.getLogo());
        vo.setIndustry(company.getIndustry());
        vo.setScale(company.getScale());
    }

    private void fillCompanyInfo(JobDetailVO vo, Company company) {
        if (company == null) {
            return;
        }
        vo.setCompanyName(company.getCompanyName());
        vo.setCompanyLogo(company.getLogo());
        vo.setIndustry(company.getIndustry());
        vo.setScale(company.getScale());
        vo.setCompanyAddress(company.getAddress());
        vo.setCompanyIntro(company.getIntro());
    }

    private void syncJobToEs(Long id) {
        try {
            jobEsService.saveOrUpdateJob(id);
        } catch (Exception e) {
            log.warn("ES岗位同步调用失败 jobId={}", id, e);
        }
    }

    private void deleteJobFromEs(Long id) {
        try {
            jobEsService.deleteJob(id);
        } catch (Exception e) {
            log.warn("ES岗位删除调用失败 jobId={}", id, e);
        }
    }
}
