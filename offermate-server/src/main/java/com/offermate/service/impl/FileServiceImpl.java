package com.offermate.service.impl;

import com.offermate.config.MinioProperties;
import com.offermate.constant.FileBizType;
import com.offermate.dto.FileDeleteDTO;
import com.offermate.dto.LoginUserDTO;
import com.offermate.exception.BusinessException;
import com.offermate.service.FileService;
import com.offermate.util.UserContext;
import com.offermate.vo.FileUploadVO;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final Set<String> FORBIDDEN_EXTENSIONS = Set.of("exe", "sh", "bat", "jar", "zip", "js", "html", "jsp", "php");
    private static final Map<String, Set<String>> MIME_TYPES = Map.of(
            "jpg", Set.of("image/jpeg"),
            "jpeg", Set.of("image/jpeg"),
            "png", Set.of("image/png"),
            "webp", Set.of("image/webp"),
            "pdf", Set.of("application/pdf"),
            "doc", Set.of("application/msword"),
            "docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    );

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public FileUploadVO upload(MultipartFile file, String bizType) {
        checkLogin();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException("请选择要上传的文件");
        }
        checkOriginalFilename(originalFilename);

        FileBizType fileBizType = FileBizType.of(bizType);
        if (fileBizType == null) {
            throw new BusinessException("文件业务类型不合法");
        }

        String ext = getExtension(originalFilename);
        if (!fileBizType.getAllowExtensions().contains(ext)) {
            throw new BusinessException("文件类型不支持");
        }
        checkMimeType(ext, file.getContentType());
        if (file.getSize() > fileBizType.getMaxSizeBytes()) {
            throw new BusinessException("文件大小不能超过 " + fileBizType.getMaxSizeMb() + "MB");
        }

        String objectName = buildObjectName(fileBizType, ext);
        try {
            ensureBucket();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return new FileUploadVO(normalizePublicUrl() + "/" + objectName);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败，请稍后再试");
        }
    }

    @Override
    public void delete(FileDeleteDTO dto) {
        checkLogin();
        if (dto == null || !StringUtils.hasText(dto.getUrl())) {
            throw new BusinessException("文件地址不能为空");
        }

        String publicUrl = normalizePublicUrl() + "/";
        String url = dto.getUrl().trim();
        if (!url.startsWith(publicUrl)) {
            throw new BusinessException("文件地址不合法");
        }
        String objectName = url.substring(publicUrl.length());
        if (!StringUtils.hasText(objectName) || objectName.contains("../") || objectName.contains("..\\")) {
            throw new BusinessException("文件地址不合法");
        }

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new BusinessException("文件删除失败，请稍后再试");
        }
    }

    private void checkLogin() {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录");
        }
    }

    private String getExtension(String filename) {
        int index = filename.lastIndexOf(".");
        if (index < 0 || index == filename.length() - 1) {
            throw new BusinessException("文件类型不支持");
        }
        String ext = filename.substring(index + 1).toLowerCase();
        if (FORBIDDEN_EXTENSIONS.contains(ext)) {
            throw new BusinessException("文件类型不支持");
        }
        return ext;
    }

    private void checkOriginalFilename(String filename) {
        if (filename.contains("../") || filename.contains("..\\") || filename.contains("/") || filename.contains("\\")) {
            throw new BusinessException("文件名不合法");
        }
    }

    private void checkMimeType(String ext, String contentType) {
        if (!StringUtils.hasText(contentType) || "application/octet-stream".equalsIgnoreCase(contentType)) {
            return;
        }
        Set<String> allowMimeTypes = MIME_TYPES.get(ext);
        if (allowMimeTypes == null || !allowMimeTypes.contains(contentType.toLowerCase())) {
            throw new BusinessException("文件类型不支持");
        }
    }

    private String buildObjectName(FileBizType fileBizType, String ext) {
        return fileBizType.getPrefix()
                + LocalDate.now().format(DATE_PATH_FORMATTER)
                + "/"
                + UUID.randomUUID().toString().replace("-", "")
                + "."
                + ext;
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.getBucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build());
        }
        setPublicReadPolicy();
    }

    private void setPublicReadPolicy() {
        try {
            String bucket = minioProperties.getBucket();
            String policy = """
                    {
                      "Version": "2012-10-17",
                      "Statement": [
                        {
                          "Effect": "Allow",
                          "Principal": "*",
                          "Action": ["s3:GetObject"],
                          "Resource": ["arn:aws:s3:::%s/*"]
                        }
                      ]
                    }
                    """.formatted(bucket);
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucket)
                    .config(policy)
                    .build());
        } catch (Exception e) {
            log.warn("MinIO bucket public-read 策略设置失败，请检查 MinIO 权限或手动配置公开访问");
        }
    }

    private String normalizePublicUrl() {
        String publicUrl = minioProperties.getPublicUrl();
        if (publicUrl.endsWith("/")) {
            return publicUrl.substring(0, publicUrl.length() - 1);
        }
        return publicUrl;
    }
}
