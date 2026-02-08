package cn.sparrowmini.common.listener;

import cn.sparrowmini.common.model.SprFile;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.apache.commons.io.FilenameUtils;

public class BaseFileEntityListener {
    @PreUpdate
    @PrePersist
    public void preSave(SprFile sprFile) {
        String ext = FilenameUtils.getExtension(sprFile.getName());
        sprFile.setFileName(String.join(".", sprFile.getHash(), ext));
    }
}
