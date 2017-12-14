package com.faendir.acra.sql.data;

import com.faendir.acra.sql.model.Attachment;
import com.faendir.acra.sql.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * @author Lukas
 * @since 11.12.2017
 */
public interface AttachmentRepository extends JpaRepository<Attachment, Attachment.MetaData> {
    List<Attachment> findAllByReport(@NonNull Report report);
}
