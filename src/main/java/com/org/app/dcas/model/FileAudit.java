package com.org.app.dcas.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "file_audit")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FileAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_audit_id")
    private Long fileAuditId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    // Add metrics columns
    @Column(name = "total_rows")
    private Integer totalRows;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "nigo_count")
    private Integer nigoCount;

    @Column(name = "unmatched_count")
    private Integer unmatchedCount;

    @Column(name = "error_count")
    private Integer errorCount;

    // Add errors list (not persisted, transient)
    @Column(name = "errors", columnDefinition = "TEXT")
    private String errors; // Store as a JSON string or comma-separated

    public List<String> getErrors() {
        if (errors == null || errors.isEmpty()) return new ArrayList<>();
        // Simple split, or use a JSON parser if stored as JSON
        return java.util.Arrays.asList(errors.split("\\|"));
    }
    public void setErrors(java.util.List<String> errorsList) {
        // Simple join, or use a JSON serializer if needed
        this.errors = String.join("|", errorsList);
    }
    
    public String getErrorsRaw() { return errors; }
    public void setErrorsRaw(String errors) { this.errors = errors; }

    // Getters and setters
    public Long getFileAuditId() { return fileAuditId; }
    public void setFileAuditId(Long fileAuditId) { this.fileAuditId = fileAuditId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public Long getId() {
        return fileAuditId;
    }

    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }

    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }

    public Integer getNigoCount() { return nigoCount; }
    public void setNigoCount(Integer nigoCount) { this.nigoCount = nigoCount; }

    public Integer getUnmatchedCount() { return unmatchedCount; }
    public void setUnmatchedCount(Integer unmatchedCount) { this.unmatchedCount = unmatchedCount; }

    public Integer getErrorCount() { return errorCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
}
