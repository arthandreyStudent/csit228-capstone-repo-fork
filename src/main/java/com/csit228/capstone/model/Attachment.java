package com.csit228.capstone.model;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Attachment implements Serializable {

    private static final long serialVersionUID = 1L;

    private int attachmentId;
    private String fileName;
    private String filePath;
    private int uploadedBy;
    private LocalDateTime uploadedAt;
    private int ticketId;

    public Attachment() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Attachment(int attachmentId, String fileName, String filePath,
                      int uploadedBy, LocalDateTime uploadedAt, int ticketId) {
        this.attachmentId = attachmentId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
        this.ticketId = ticketId;
    }

    public boolean upload() {
        //iconnect ni sa AttachmentDAO or actual file upload logic
        return filePath != null && !filePath.trim().isEmpty();
    }

    public File download() {
        //improve later if file handling is added
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }

        return new File(filePath);
    }

    public boolean delete() {
        //iconnect ni sa AttachmentDAO or actual file delete logic
        return attachmentId > 0;
    }

    public int getAttachmentId() {
        return attachmentId;
    }

    public int getId() {
        return attachmentId;
    }

    public void setAttachmentId(int attachmentId) {
        this.attachmentId = attachmentId;
    }

    public void setId(int attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(int uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    @Override
    public String toString() {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "Attachment " + attachmentId;
        }

        return fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Attachment)) {
            return false;
        }

        Attachment that = (Attachment) o;
        return attachmentId == that.attachmentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(attachmentId);
    }
}