package com.org.app.dcas.controller;

import com.org.app.dcas.service.MasterDataUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/master-data")
public class MasterDataUploadController {

    private final MasterDataUploadService masterDataUploadService;

    @Autowired
    public MasterDataUploadController(MasterDataUploadService masterDataUploadService) {
        this.masterDataUploadService = masterDataUploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadMasterData(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = masterDataUploadService.processMasterDataExcel(file);
        return ResponseEntity.ok(result);
    }
}
