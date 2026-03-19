package com.ritesh.scalablefileupload.controller;

import com.ritesh.scalablefileupload.model.MultiPart;
import com.ritesh.scalablefileupload.service.MultiPartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.List;
import java.util.Map;

@RestController
public class MultiPartController {
    @Autowired
    private MultiPartService multiPartService;

    @PostMapping("/files/multipart")
    public ResponseEntity<Map<String,Object>> startMultiPart(@RequestBody Map<String,String>file){

        String fileName = file.get("fileName");
        String contentType = file.get("contentType");
        int totalParts = Integer.parseInt(file.get("totalParts"));

        Map<String,Object> response  = multiPartService.startMultiPartUpload(fileName,contentType,totalParts);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/files/multipart/{id}")
    public ResponseEntity<List<MultiPart>> getMultiParts(@PathVariable Long id){
        List<MultiPart> response = multiPartService.getMultiParts(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping("/files/multipart/api")
    public ResponseEntity<Map<String,String>> confirmMultiPart(@RequestBody Map<String,String> body){

        System.out.println("Hit");

        Long fileId = Long.parseLong(body.get("fileId"));
        int partNumber = Integer.parseInt(body.get("partNumber"));
        String eTag = body.get("eTag");

        System.out.println(fileId);
        System.out.println(partNumber);
        System.out.println(eTag);

        Map<String,String> map = multiPartService.confirmMultiPart(fileId,partNumber,eTag);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/files/multipart/{fileId}/{partNumber}")
    public ResponseEntity<MultiPart> getMultiPart(@PathVariable Long fileId, @PathVariable  int partNumber){
        MultiPart response  = multiPartService.getMultiPart(fileId,partNumber);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/files/multipart/{fileId}")
    public ResponseEntity<Map<String,String>> deleteMultiPart(@PathVariable Long fileId, @RequestBody Map<String,String> body){

        String uploadId = body.get("uploadId");
        String key = body.get("key");
        System.out.println("Hit");

        Map<String ,String> response  = multiPartService.abortMultiPartUpload(uploadId,key,fileId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

//    @PostMapping("/files/multipart/{fileId}")
//    public ResponseEntity<Map<String,String>> confirmMultiPart(@PathVariable Long fileId, @RequestBody Map<String,String> body){
//
//        String uploadId = body.get("uploadId");
//        String key = body.get("key");
////        List<CompletedPart> parts = (List<CompletedPart>) body.get("parts");
//        System.out.println("Hit");
//
//        Map<String ,String> response  = multiPartService.abortMultiPartUpload(uploadId,key,fileId);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }

}
