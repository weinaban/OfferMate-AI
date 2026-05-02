package com.offermate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offermate.common.Result;
import com.offermate.common.result.PageResult;
import com.offermate.dto.LoginUserDTO;
import com.offermate.dto.OperationLogQueryDTO;
import com.offermate.entity.OperationLog;
import com.offermate.exception.BusinessException;
import com.offermate.mapper.OperationLogMapper;
import com.offermate.service.OperationLogService;
import com.offermate.util.PageUtils;
import com.offermate.util.UserContext;
import com.offermate.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    private static final int ADMIN_ROLE = 3;

    @Override
    public void saveLog(OperationLog logRecord) {
        if (logRecord == null) {
            return;
        }
        try {
            save(logRecord);
        } catch (Exception e) {
            log.error("操作日志保存失败", e);
        }
    }

    @Override
    public PageResult<OperationLogVO> pageLogs(OperationLogQueryDTO dto) {
        checkAdmin();
        if (dto == null) {
            dto = new OperationLogQueryDTO();
        }
        int page = PageUtils.page(dto.getPage());
        int pageSize = PageUtils.pageSize(dto.getPageSize());
        Page<OperationLog> pageInfo = page(new Page<>(page, pageSize),
                new LambdaQueryWrapper<OperationLog>()
                        .eq(dto.getUserId() != null, OperationLog::getUserId, dto.getUserId())
                        .like(StringUtils.hasText(dto.getUsername()), OperationLog::getUsername, dto.getUsername())
                        .eq(StringUtils.hasText(dto.getModule()), OperationLog::getModule, dto.getModule())
                        .like(StringUtils.hasText(dto.getOperation()), OperationLog::getOperation, dto.getOperation())
                        .eq(dto.getStatus() != null, OperationLog::getStatus, dto.getStatus())
                        .ge(dto.getStartTime() != null, OperationLog::getCreateTime, dto.getStartTime())
                        .le(dto.getEndTime() != null, OperationLog::getCreateTime, dto.getEndTime())
                        .orderByDesc(OperationLog::getCreateTime));
        List<OperationLogVO> records = pageInfo.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(pageInfo.getTotal(), records);
    }

    private void checkAdmin() {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getRole() == null || loginUser.getRole() != ADMIN_ROLE) {
            throw new BusinessException(Result.FORBIDDEN_CODE, "无权限");
        }
    }

    private OperationLogVO toVO(OperationLog logRecord) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(logRecord.getId());
        vo.setUserId(logRecord.getUserId());
        vo.setUsername(logRecord.getUsername());
        vo.setRole(logRecord.getRole());
        vo.setModule(logRecord.getModule());
        vo.setOperation(logRecord.getOperation());
        vo.setMethod(logRecord.getMethod());
        vo.setRequestUri(logRecord.getRequestUri());
        vo.setRequestMethod(logRecord.getRequestMethod());
        vo.setIp(logRecord.getIp());
        vo.setParams(logRecord.getParams());
        vo.setResult(logRecord.getResult());
        vo.setStatus(logRecord.getStatus());
        vo.setErrorMsg(logRecord.getErrorMsg());
        vo.setCostTime(logRecord.getCostTime());
        vo.setCreateTime(logRecord.getCreateTime());
        return vo;
    }
}
