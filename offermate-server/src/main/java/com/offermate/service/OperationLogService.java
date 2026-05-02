package com.offermate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offermate.common.result.PageResult;
import com.offermate.dto.OperationLogQueryDTO;
import com.offermate.entity.OperationLog;
import com.offermate.vo.OperationLogVO;

public interface OperationLogService extends IService<OperationLog> {

    void saveLog(OperationLog logRecord);

    PageResult<OperationLogVO> pageLogs(OperationLogQueryDTO dto);
}
