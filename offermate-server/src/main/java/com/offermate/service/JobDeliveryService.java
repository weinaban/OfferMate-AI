package com.offermate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offermate.dto.DeliveryCreateDTO;
import com.offermate.dto.DeliveryStatusDTO;
import com.offermate.entity.JobDelivery;
import com.offermate.vo.DeliveryVO;

import java.util.List;

public interface JobDeliveryService extends IService<JobDelivery> {

    void deliverJob(DeliveryCreateDTO dto);

    List<DeliveryVO> listMyDeliveries();

    List<DeliveryVO> listCompanyDeliveries();

    void updateDeliveryStatus(Long id, DeliveryStatusDTO dto);

    void deleteMyDelivery(Long id);
}
