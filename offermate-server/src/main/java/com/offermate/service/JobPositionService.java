package com.offermate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offermate.common.result.PageResult;
import com.offermate.dto.JobCreateDTO;
import com.offermate.dto.JobPageQueryDTO;
import com.offermate.dto.JobSearchDTO;
import com.offermate.dto.JobUpdateDTO;
import com.offermate.entity.JobPosition;
import com.offermate.vo.JobDetailVO;
import com.offermate.vo.JobPageVO;

import java.util.List;

public interface JobPositionService extends IService<JobPosition> {

    void createJob(JobCreateDTO dto);

    void updateJob(Long id, JobUpdateDTO dto);

    void deleteJob(Long id);

    void offlineJob(Long id);

    JobDetailVO getJobDetail(Long id);

    PageResult<JobPageVO> pageJobs(JobPageQueryDTO dto);

    PageResult<JobPageVO> searchJobs(JobSearchDTO dto);

    int syncJobsToEs();

    List<JobPageVO> listCompanyJobs();
}
