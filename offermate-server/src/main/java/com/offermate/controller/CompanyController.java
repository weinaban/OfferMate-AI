package com.offermate.controller;

import com.offermate.common.Result;
import com.offermate.dto.CompanySaveDTO;
import com.offermate.dto.FileUrlDTO;
import com.offermate.exception.BusinessException;
import com.offermate.service.CompanyService;
import com.offermate.vo.CompanyVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@Validated
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/info")
    public Result<CompanyVO> getMyCompany() {
        return Result.success(companyService.getMyCompany());
    }

    @PutMapping("/info")
    public Result<Void> saveMyCompany(@Valid @RequestBody CompanySaveDTO dto) {
        companyService.saveMyCompany(dto);
        return Result.success();
    }

    @PostMapping("/logo")
    public Result<Void> updateLogo(@Valid @RequestBody FileUrlDTO dto) {
        String logo = dto == null ? null : dto.getLogo();
        if (!StringUtils.hasText(logo) && dto != null) {
            logo = dto.getUrl();
        }
        if (!StringUtils.hasText(logo)) {
            throw new BusinessException("企业Logo地址不能为空");
        }
        companyService.updateLogo(logo);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<CompanyVO> getCompanyDetail(@PathVariable @Min(value = 1, message = "企业ID不合法") Long id) {
        return Result.success(companyService.getCompanyDetail(id));
    }
}
