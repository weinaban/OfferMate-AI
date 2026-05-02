package com.offermate.service;

import com.offermate.common.result.PageResult;
import com.offermate.dto.JobSearchDTO;
import com.offermate.vo.JobPageVO;

public interface JobEsService {

    void createIndexIfNotExists();

    void saveOrUpdateJob(Long jobId);

    void deleteJob(Long jobId);

    int syncAll();

    PageResult<JobPageVO> searchJobs(JobSearchDTO dto);
}
