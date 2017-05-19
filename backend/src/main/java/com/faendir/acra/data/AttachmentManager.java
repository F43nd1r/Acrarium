package com.faendir.acra.data;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.gridfs.GridFSDBFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author Lukas
 * @since 19.05.2017
 */
@Component
public class AttachmentManager {
    private final GridFsTemplate gridFsTemplate;
    private final Logger logger;

    @Autowired
    public AttachmentManager(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
        logger = LoggerFactory.getLogger(AttachmentManager.class);
    }

    public void saveAttachments(String report, List<MultipartFile> attachments) {
        for (MultipartFile a : attachments) {
            try {
                gridFsTemplate.store(a.getInputStream(), a.getOriginalFilename(), a.getContentType(), new BasicDBObjectBuilder().add("reportId", report).get());
            } catch (IOException e) {
                logger.warn("Failed to load attachment", e);
            }
        }
    }

    public List<GridFSDBFile> getAttachments(String report) {
        return gridFsTemplate.find(new Query(Criteria.where("metadata.reportId").is(report)));
    }

    public void removeAttachments(String report) {
        gridFsTemplate.delete(new Query(Criteria.where("metadata.reportId").is(report)));
    }
}
