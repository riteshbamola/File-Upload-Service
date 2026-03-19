package com.ritesh.scalablefileupload.service;

import com.ritesh.scalablefileupload.enums.FileStatus;
import com.ritesh.scalablefileupload.enums.PartStatus;
import com.ritesh.scalablefileupload.model.File;
import com.ritesh.scalablefileupload.model.MultiPart;
import com.ritesh.scalablefileupload.model.User;
import com.ritesh.scalablefileupload.repo.FileRepo;

import com.ritesh.scalablefileupload.repo.MultiPartRepo;
import com.ritesh.scalablefileupload.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class MultiPartService {
    @Autowired
    private AwsService awsService;

    @Autowired
    private FileRepo fileRepo;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private MultiPartRepo multiPartRepo;

    @Value("${aws.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String region;



    private String generateKey(String fileName, Long userId) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        long timestamp = System.currentTimeMillis();
        return "uploads/" + userId + "/" + timestamp + "." + extension;
    }

    public Map<String,Object> startMultiPartUpload(String fileName, String contentType, int totalParts){
        User currentUser = authUtil.getCurrentUser();
        String key  = generateKey(fileName,currentUser.getUserId());

        Map<String,Object> res =awsService.startUpload(key,contentType,totalParts);

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

        for(int i=1;i<=totalParts;i++){
            MultiPart multiPart = new MultiPart();
            multiPart.setPartNumber(i);
            multiPart.setFile(fileEntity);
            multiPartRepo.save(multiPart);
        }
        return res;


    }
//    public Map<String,String> confirmMultiPart()



    public List<MultiPart> getMultiParts(Long fileId){
        File file = fileRepo.findById(fileId).orElseThrow(()->new RuntimeException("File Not Found"));
        List<MultiPart> multiParts = multiPartRepo.findByFile(file);
        return multiParts;
    }

    public Map<String,String> confirmMultiPart(Long fileId, int partNumber,String eTag) {
        File file = fileRepo.findById(fileId).orElseThrow(()->new RuntimeException("File Not Found"));
        MultiPart multiPart = multiPartRepo.findByFileAndPartNumber(file,partNumber);
        multiPart.setStatus(PartStatus.UPLOADED);
        multiPart.setUpdatedAt(LocalDateTime.now());
        multiPart.seteTag(eTag);
        multiPartRepo.save(multiPart);
        return Map.of(
                "message","MultiPart Uploaded Succesfully"
        );

    }
    public MultiPart getMultiPart(Long fileId, int partNumber) {
        File file = fileRepo.findById(fileId).orElseThrow(()->new RuntimeException("File Not Found"));
        MultiPart multiPart = multiPartRepo.findByFileAndPartNumber(file,partNumber);
        return multiPart;
    }
    @Transactional
    public Map<String,String> abortMultiPartUpload(String uploadId,String key,Long fileId){
        File file = fileRepo.findById(fileId).orElseThrow(()->new RuntimeException("File Not Found"));

        awsService.abortMultipartUpload(key,uploadId);
        multiPartRepo.deleteByFile(file);
        file.setStatus(FileStatus.FAILED);

        fileRepo.save(file);


        String response = "MultiPart of File "+file.getId()+" Deleted";
        return Map.of(
                "message1", response,
                "message2","File Upload Failed"
        );
    }
    public Map<String, String> confirmUpload(Long fileId){
        User currentUser = authUtil.getCurrentUser();

        File file = fileRepo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));


        if (file.getOwner().getUserId()!=(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized");
        }
//        awsService.completeUpload()   //yet to complete
        file.setStatus(FileStatus.UPLOADED);
        file.setUpdatedAt(LocalDateTime.now());
        fileRepo.save(file);

        return Map.of(
                "message", "File uploaded successfully",
                "url", awsService.getFileUrl(file.getStorageKey())
        );
    }

}
