package com.ritesh.scalablefileupload.service;

import com.ritesh.scalablefileupload.enums.FileStatus;
import com.ritesh.scalablefileupload.model.File;
import com.ritesh.scalablefileupload.model.User;
import com.ritesh.scalablefileupload.repo.FileRepo;
import com.ritesh.scalablefileupload.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private AwsService awsService;

    @Autowired
    private FileRepo fileRepo;

    @Autowired
    private AuthUtil authUtil;

    @Value("${aws.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String region;


    private String generateKey(String fileName, Long userId) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        long timestamp = System.currentTimeMillis();
        return "uploads/" + userId + "/" + timestamp + "." + extension;
    }

    public Map<String, String> getPresignedUrl(String fileName, String contentType) {
        User currentUser = authUtil.getCurrentUser();

        String key = generateKey(fileName,currentUser.getUserId());


        String presignedUrl = awsService.generatePresignedUrl(key, contentType);


        File fileEntity = new File();
        fileEntity.setOwner(currentUser);
        fileEntity.setSize(500L);
        fileEntity.setOriginalName(fileName);
        fileEntity.setMimeType(contentType);
        fileEntity.setStorageKey(key);
        fileEntity.setStorageBucket(bucket);
        fileEntity.setStorageRegion(region);
        fileEntity.setStatus(FileStatus.PENDING);
        fileRepo.save(fileEntity);

        return Map.of(
                "presignedUrl", presignedUrl,
                "key", key,
                "fileId", String.valueOf(fileEntity.getId())
        );
    }


    public Map<String, String> confirmUpload(Long fileId){
        User currentUser = authUtil.getCurrentUser();

        File file = fileRepo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));


        if (file.getOwner().getUserId()!=(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized");
        }

        file.setStatus(FileStatus.UPLOADED);
        file.setUpdatedAt(LocalDateTime.now());
        fileRepo.save(file);

        return Map.of(
                "message", "File uploaded successfully",
                "url", awsService.getFileUrl(file.getStorageKey())
        );
    }

    public List<File> getFiles() {
        User user= authUtil.getCurrentUser();
        List<File> files = fileRepo.findByOwner(user);
        return files;
    }

    public File getFile(Long fileId){
        File file = fileRepo.findById(fileId).orElseThrow(() -> new RuntimeException("File Not Found"));
        return file;
    }

    public void deleteFile(Long fileId){

        File file = fileRepo.findById(fileId).orElseThrow(()-> new RuntimeException("File not Found"));

        String key = file.getStorageKey();
        awsService.deleteFile(key);
        fileRepo.delete(file);
    }

}