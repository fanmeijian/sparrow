package cn.sparrowmini.common.service;

import cn.sparrowmini.common.exception.BusinessLogicException;
import cn.sparrowmini.common.exception.DenyPermissionException;
import cn.sparrowmini.common.exception.NoPermissionException;
import cn.sparrowmini.common.model.BaseFile;

import java.util.Map;

/**
 * 文件的下载和上传权限
 */
public interface FilePermissionService {
    public boolean canUpload(Map<String, Object> params) throws NoPermissionException, DenyPermissionException, BusinessLogicException;
    public boolean canDownload(BaseFile file, Map<String, Object> params) throws NoPermissionException, DenyPermissionException, BusinessLogicException;
}
