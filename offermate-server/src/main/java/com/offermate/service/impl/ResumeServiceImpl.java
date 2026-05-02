package com.offermate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offermate.dto.LoginUserDTO;
import com.offermate.dto.ResumeCreateDTO;
import com.offermate.dto.ResumeUpdateDTO;
import com.offermate.entity.Resume;
import com.offermate.exception.BusinessException;
import com.offermate.mapper.ResumeMapper;
import com.offermate.service.ResumeService;
import com.offermate.util.UserContext;
import com.offermate.vo.ResumeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {

    private static final int DEFAULT_YES = 1;
    private static final int DEFAULT_NO = 0;

    @Override
    public void createResume(ResumeCreateDTO dto) {
        Long userId = getLoginUserId();
        if (dto == null || !StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException("简历名称不能为空");
        }
        checkExperienceYear(dto.getExperienceYear());

        long count = count(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, userId));

        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(trim(dto.getTitle()));
        resume.setRealName(trim(dto.getRealName()));
        resume.setPhone(trim(dto.getPhone()));
        resume.setEmail(trim(dto.getEmail()));
        resume.setEducation(trim(dto.getEducation()));
        resume.setExperienceYear(dto.getExperienceYear());
        resume.setSkill(trim(dto.getSkill()));
        resume.setProjectExp(trim(dto.getProjectExp()));
        resume.setSelfIntro(trim(dto.getSelfIntro()));
        resume.setIsDefault(count == 0 ? DEFAULT_YES : DEFAULT_NO);
        save(resume);
    }

    @Override
    public void updateResume(Long id, ResumeUpdateDTO dto) {
        Long userId = getLoginUserId();
        Resume resume = checkResumeOwner(id, userId);
        if (dto == null || !StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException("简历名称不能为空");
        }
        checkExperienceYear(dto.getExperienceYear());

        resume.setTitle(trim(dto.getTitle()));
        resume.setRealName(trim(dto.getRealName()));
        resume.setPhone(trim(dto.getPhone()));
        resume.setEmail(trim(dto.getEmail()));
        resume.setEducation(trim(dto.getEducation()));
        resume.setExperienceYear(dto.getExperienceYear());
        resume.setSkill(trim(dto.getSkill()));
        resume.setProjectExp(trim(dto.getProjectExp()));
        resume.setSelfIntro(trim(dto.getSelfIntro()));
        updateById(resume);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResume(Long id) {
        Long userId = getLoginUserId();
        Resume resume = checkResumeOwner(id, userId);
        removeById(id);

        if (resume.getIsDefault() != null && resume.getIsDefault() == DEFAULT_YES) {
            Resume latestResume = getOne(new LambdaQueryWrapper<Resume>()
                    .eq(Resume::getUserId, userId)
                    .orderByDesc(Resume::getCreateTime)
                    .last("LIMIT 1"), false);
            if (latestResume != null) {
                latestResume.setIsDefault(DEFAULT_YES);
                updateById(latestResume);
            }
        }
    }

    @Override
    public ResumeVO getResumeDetail(Long id) {
        Long userId = getLoginUserId();
        return toVO(checkResumeOwner(id, userId));
    }

    @Override
    public List<ResumeVO> listMyResumes() {
        Long userId = getLoginUserId();
        return list(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, userId)
                .orderByDesc(Resume::getIsDefault)
                .orderByDesc(Resume::getCreateTime))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultResume(Long id) {
        Long userId = getLoginUserId();
        checkResumeOwner(id, userId);

        update(new LambdaUpdateWrapper<Resume>()
                .eq(Resume::getUserId, userId)
                .set(Resume::getIsDefault, DEFAULT_NO));
        update(new LambdaUpdateWrapper<Resume>()
                .eq(Resume::getId, id)
                .eq(Resume::getUserId, userId)
                .set(Resume::getIsDefault, DEFAULT_YES));
    }

    private Long getLoginUserId() {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录");
        }
        return loginUser.getUserId();
    }

    private Resume checkResumeOwner(Long id, Long userId) {
        Resume resume = getById(id);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该简历");
        }
        return resume;
    }

    private void checkExperienceYear(Integer experienceYear) {
        if (experienceYear != null && experienceYear < 0) {
            throw new BusinessException("工作年限不能小于0");
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private ResumeVO toVO(Resume resume) {
        ResumeVO vo = new ResumeVO();
        vo.setId(resume.getId());
        vo.setUserId(resume.getUserId());
        vo.setTitle(resume.getTitle());
        vo.setRealName(resume.getRealName());
        vo.setPhone(resume.getPhone());
        vo.setEmail(resume.getEmail());
        vo.setEducation(resume.getEducation());
        vo.setExperienceYear(resume.getExperienceYear());
        vo.setSkill(resume.getSkill());
        vo.setProjectExp(resume.getProjectExp());
        vo.setSelfIntro(resume.getSelfIntro());
        vo.setIsDefault(resume.getIsDefault());
        vo.setCreateTime(resume.getCreateTime());
        vo.setUpdateTime(resume.getUpdateTime());
        return vo;
    }
}
