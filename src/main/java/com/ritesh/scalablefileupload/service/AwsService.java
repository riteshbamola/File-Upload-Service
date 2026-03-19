package com.ritesh.scalablefileupload.service;

import com.ritesh.scalablefileupload.model.User;
import com.ritesh.scalablefileupload.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import software.amazon.awssdk.services.s3.presigner.model.*;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AwsService {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String region;

    @Autowired
    private S3Presigner s3Presigner;


    public String generatePresignedUrl(String key, String contentType) {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return presignedRequest.url().toString();
    }

    public String getFileUrl(String key) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }


    public void deleteFile(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
    }


    public List<String> generatePresignedUrlsForAllParts(String key, String uploadId, int totalParts) {
        List<String> presignedUrls = new ArrayList<>();

        for (int partNumber = 1; partNumber <= totalParts; partNumber++) {
            UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .build();

            UploadPartPresignRequest uploadPartPresignRequest = UploadPartPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .uploadPartRequest(uploadPartRequest)
                    .build();

            PresignedUploadPartRequest presignedUploadPartRequest =
                    s3Presigner.presignUploadPart(uploadPartPresignRequest);

            presignedUrls.add(presignedUploadPartRequest.url().toString());
        }

        return presignedUrls;
    }

    public Map<String, Object> startUpload(String key, String contentType, int totalParts) {

        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        CreateMultipartUploadResponse uploadResponse =
                s3Client.createMultipartUpload(createMultipartUploadRequest);

        String uploadId = uploadResponse.uploadId();

        List<String> presignedUrls = generatePresignedUrlsForAllParts(key, uploadId, totalParts);

        return Map.of(
                "uploadId", uploadId,
                "key", key,
                "presignedUrls", presignedUrls
        );
    }

    public String completeUpload(String key, String uploadId, List<CompletedPart> parts) {

        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(parts)
                .build();

        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                CompleteMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .uploadId(uploadId)
                        .multipartUpload(completedMultipartUpload)
                        .build();

        CompleteMultipartUploadResponse response =
                s3Client.completeMultipartUpload(completeMultipartUploadRequest);

        return response.location();
    }

    public void abortMultipartUpload(String key, String uploadId) {
        AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .build();

        s3Client.abortMultipartUpload(abortRequest);
    }


}