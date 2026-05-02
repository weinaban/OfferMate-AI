package com.offermate.controller;

import com.offermate.common.Result;
import com.offermate.annotation.OperationLog;
import com.offermate.dto.FileDeleteDTO;
import com.offermate.service.FileService;
import com.offermate.vo.FileUploadVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Validated
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @OperationLog(module = "文件管理", operation = "文件上传")
    public Result<FileUploadVO> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam("bizType") @NotBlank(message = "文件业务类型不能为空") String bizType) {
        return Result.success(fileService.upload(file, bizType));
    }

    @DeleteMapping
    @OperationLog(module = "文件管理", operation = "删除文件")
    public Result<Void> delete(@Valid @RequestBody FileDeleteDTO dto) {
        fileService.delete(dto);
        return Result.success();
    }
}
