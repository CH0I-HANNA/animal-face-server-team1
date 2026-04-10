package com.likelion.animalface.domain.analysis.service;

import com.likelion.animalface.domain.analysis.dto.req.PresignedUrlReq;
import com.likelion.animalface.domain.analysis.dto.res.PresignedUrlRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

/**
 * S3 스토리지 서비스 (/api/v1/storage)
 */
@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    /** S3 Presigned PUT URL 발급 */
    public PresignedUrlRes generatePresignedUrl(PresignedUrlReq req) {
        String key = "uploads/" + UUID.randomUUID() + "/" + req.fileName();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(req.contentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

        String presignedUrl = presigned.url().toString();
        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

        return PresignedUrlRes.of(presignedUrl, fileUrl);
    }
}
