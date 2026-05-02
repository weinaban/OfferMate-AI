package com.offermate.service;

import com.offermate.dto.FileDeleteDTO;
import com.offermate.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadVO upload(MultipartFile file, String bizType);

    void delete(FileDeleteDTO dto);
}
