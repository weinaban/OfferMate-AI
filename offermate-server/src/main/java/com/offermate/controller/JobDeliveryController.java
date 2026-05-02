package com.offermate.controller;

import com.offermate.common.Result;
import com.offermate.annotation.OperationLog;
import com.offermate.dto.DeliveryCreateDTO;
import com.offermate.dto.DeliveryStatusDTO;
import com.offermate.service.JobDeliveryService;
import com.offermate.vo.DeliveryVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Validated
public class JobDeliveryController {

    private final JobDeliveryService jobDeliveryService;

    @PostMapping
    @OperationLog(module = "投递管理", operation = "投递岗位")
    public Result<Void> deliverJob(@Valid @RequestBody DeliveryCreateDTO dto) {
        jobDeliveryService.deliverJob(dto);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<DeliveryVO>> listMyDeliveries() {
        return Result.success(jobDeliveryService.listMyDeliveries());
    }

    @GetMapping("/company")
    public Result<List<DeliveryVO>> listCompanyDeliveries() {
        return Result.success(jobDeliveryService.listCompanyDeliveries());
    }

    @PutMapping("/{id}/status")
    @OperationLog(module = "投递管理", operation = "修改投递状态")
    public Result<Void> updateDeliveryStatus(@PathVariable @Min(value = 1, message = "投递记录ID不合法") Long id,
                                             @Valid @RequestBody DeliveryStatusDTO dto) {
        jobDeliveryService.updateDeliveryStatus(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMyDelivery(@PathVariable @Min(value = 1, message = "投递记录ID不合法") Long id) {
        jobDeliveryService.deleteMyDelivery(id);
        return Result.success();
    }
}
