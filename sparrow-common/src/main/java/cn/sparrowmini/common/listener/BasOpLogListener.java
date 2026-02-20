package cn.sparrowmini.common.listener;

import cn.sparrowmini.common.CurrentUser;
import cn.sparrowmini.common.model.BaseOpLog;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class BasOpLogListener {

    @PrePersist
    private void preSave(BaseOpLog opLog) {
        opLog.setCreatedBy(CurrentUser.get()) ;
        opLog.setModifiedBy(CurrentUser.get());
    }

    @PreUpdate
    private void preUpdate(BaseOpLog opLog){
        opLog.setModifiedBy(CurrentUser.get());
    }
}
