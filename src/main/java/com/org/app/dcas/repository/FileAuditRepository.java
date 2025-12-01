package com.org.app.dcas.repository;

import com.org.app.dcas.model.FileAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileAuditRepository extends JpaRepository<FileAudit, Long> {
}
