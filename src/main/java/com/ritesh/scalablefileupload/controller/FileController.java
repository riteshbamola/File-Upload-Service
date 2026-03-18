package com.ritesh.scalablefileupload.controller;

import com.ritesh.scalablefileupload.model.File;
import com.ritesh.scalablefileupload.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/files")
    public ResponseEntity<?> uploadFile(@RequestBody Map<String,String>file){

        String fileName = file.get("fileName");
        String contentType = file.get("contentType");
        Map<String,String> response  = fileService.getPresignedUrl(fileName,contentType);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
