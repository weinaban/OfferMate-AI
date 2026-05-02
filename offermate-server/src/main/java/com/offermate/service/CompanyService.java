package com.offermate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offermate.dto.CompanySaveDTO;
import com.offermate.entity.Company;
import com.offermate.vo.CompanyVO;

public interface CompanyService extends IService<Company> {

    CompanyVO getMyCompany();

    void saveMyCompany(CompanySaveDTO dto);

    CompanyVO getCompanyDetail(Long id);

    Company getByUserId(Long userId);

    void updateLogo(String logo);
}
