package com.ritesh.scalablefileupload.controller;

import com.ritesh.scalablefileupload.model.File;
import com.ritesh.scalablefileupload.service.FileService;
import com.ritesh.scalablefileupload.service.MultiPartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @PutMapping("/files/{fileId}")
    public ResponseEntity<Map<String, String>> confirmUpload(@PathVariable Long fileId) {
        Map<String, String> response = fileService.confirmUpload(fileId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/files")
    public ResponseEntity<List<File>> getFiles(){
         List<File>files = fileService.getFiles();
         return ResponseEntity.status(HttpStatus.OK).body(files);

    }

    @GetMapping("/files/{id}")
    public ResponseEntity<File> getFile(@PathVariable Long id){

        File file = fileService.getFile(id);
        return ResponseEntity.status(HttpStatus.OK).body(file);
    }

    @DeleteMapping("/files/{id}")
    public ResponseEntity<Map<String,String>> deleteFile(@PathVariable Long id){

        fileService.deleteFile(id);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "accessToken", "File Deleted Succesfully"
        ));
    }





}
