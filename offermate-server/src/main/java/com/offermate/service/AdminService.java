package com.offermate.service;

import com.offermate.common.result.PageResult;
import com.offermate.dto.AdminCompanyAuditPageQueryDTO;
import com.offermate.dto.AdminJobAuditPageQueryDTO;
import com.offermate.dto.AdminUserPageQueryDTO;
import com.offermate.dto.AuditDTO;
import com.offermate.vo.AdminCompanyAuditVO;
import com.offermate.vo.AdminJobAuditVO;
import com.offermate.vo.AdminUserVO;

public interface AdminService {

    PageResult<AdminUserVO> pageUsers(AdminUserPageQueryDTO dto);

    void disableUser(Long id);

    void enableUser(Long id);

    PageResult<AdminCompanyAuditVO> pageCompanyAudits(AdminCompanyAuditPageQueryDTO dto);

    void auditCompany(Long id, AuditDTO dto);

    PageResult<AdminJobAuditVO> pageJobAudits(AdminJobAuditPageQueryDTO dto);

    void auditJob(Long id, AuditDTO dto);
}
