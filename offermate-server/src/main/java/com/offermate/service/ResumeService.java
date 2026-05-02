package com.offermate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offermate.dto.ResumeCreateDTO;
import com.offermate.dto.ResumeUpdateDTO;
import com.offermate.entity.Resume;
import com.offermate.vo.ResumeVO;

import java.util.List;

public interface ResumeService extends IService<Resume> {

    void createResume(ResumeCreateDTO dto);

    void updateResume(Long id, ResumeUpdateDTO dto);

    void deleteResume(Long id);

    ResumeVO getResumeDetail(Long id);

    List<ResumeVO> listMyResumes();

    void setDefaultResume(Long id);
}
