package com.offermate.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.offermate.annotation.OperationLog;
import com.offermate.dto.LoginUserDTO;
import com.offermate.dto.UserLoginDTO;
import com.offermate.service.OperationLogService;
import com.offermate.util.IpUtils;
import com.offermate.util.LogUtils;
import com.offermate.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final int SUCCESS = 1;
    private static final int FAIL = 0;

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable throwable = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            throwable = e;
            throw e;
        } finally {
            saveOperationLog(joinPoint, operationLog, result, throwable, System.currentTimeMillis() - start);
        }
    }

    private void saveOperationLog(ProceedingJoinPoint joinPoint, OperationLog operationLog,
                                  Object result, Throwable throwable, long costTime) {
        try {
            com.offermate.entity.OperationLog logRecord = new com.offermate.entity.OperationLog();
            LoginUserDTO loginUser = UserContext.getUser();
            if (loginUser != null) {
                logRecord.setUserId(loginUser.getUserId());
                logRecord.setUsername(loginUser.getUsername());
                logRecord.setRole(loginUser.getRole());
            } else {
                fillLoginUsername(joinPoint, logRecord);
            }
            HttpServletRequest request = getRequest();
            if (request != null) {
                logRecord.setRequestUri(request.getRequestURI());
                logRecord.setRequestMethod(request.getMethod());
                logRecord.setIp(IpUtils.getIp(request));
            }
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            logRecord.setModule(operationLog.module());
            logRecord.setOperation(operationLog.operation());
            logRecord.setMethod(signature.getDeclaringType().getSimpleName() + "." + signature.getName());
            logRecord.setParams(buildParams(joinPoint.getArgs()));
            logRecord.setResult(throwable == null ? LogUtils.toSafeJson(objectMapper, result) : null);
            logRecord.setStatus(throwable == null ? SUCCESS : FAIL);
            logRecord.setErrorMsg(throwable == null ? null : LogUtils.truncate(throwable.getMessage()));
            logRecord.setCostTime(costTime);
            logRecord.setCreateTime(LocalDateTime.now());
            operationLogService.saveLog(logRecord);
        } catch (Exception e) {
            log.error("操作日志记录失败", e);
        }
    }

    private String buildParams(Object[] args) {
        List<Object> params = new ArrayList<>();
        if (args != null) {
            for (Object arg : args) {
                Object safeArg = LogUtils.sanitizeArg(arg);
                if (safeArg != null) {
                    params.add(safeArg);
                }
            }
        }
        return LogUtils.toSafeJson(objectMapper, params);
    }

    private void fillLoginUsername(ProceedingJoinPoint joinPoint, com.offermate.entity.OperationLog logRecord) {
        Object[] args = joinPoint.getArgs();
        if (args == null) {
            return;
        }
        for (Object arg : args) {
            if (arg instanceof UserLoginDTO loginDTO) {
                logRecord.setUsername(loginDTO.getUsername());
                return;
            }
        }
    }

    private HttpServletRequest getRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
