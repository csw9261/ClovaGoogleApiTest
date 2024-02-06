package com.clovagoogleapitest.Google;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class FileUploadController {
    private static final String UPLOAD_DIR = "C:/Users/User/Downloads/";
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Failed to upload empty file";
        }

        try {
            // 파일 이름 가져오기
            String fileName = file.getOriginalFilename();
            // 저장할 경로를 Path 객체로 생성
            Path path = Paths.get(UPLOAD_DIR + fileName);
            // 파일을 저장
            Files.copy(file.getInputStream(), path);

            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to upload file";
        }
    }
}
