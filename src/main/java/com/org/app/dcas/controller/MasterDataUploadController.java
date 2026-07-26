package com.org.app.dcas.controller;

import com.org.app.dcas.service.MasterDataUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/master-data")
public class MasterDataUploadController {

    private static final Logger log = LoggerFactory.getLogger(MasterDataUploadController.class);

    private final MasterDataUploadService masterDataUploadService;

    @Autowired
    public MasterDataUploadController(MasterDataUploadService masterDataUploadService) {
        this.masterDataUploadService = masterDataUploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadMasterData(@RequestParam("file") MultipartFile file) {
        log.info("POST /api/master-data/upload - processing file='{}', size={} bytes",
                file.getOriginalFilename(), file.getSize());
        Map<String, Object> result = masterDataUploadService.processMasterDataExcel(file);
        log.info("POST /api/master-data/upload - completed, result={}", result);
        return ResponseEntity.ok(result);
    }
}
