package cn.sparrowmini.common.service;

import cn.sparrowmini.common.constant.StorageTypeEnum;
import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.common.model.BaseFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface StorageService {
    public <T extends BaseFile> byte[] download(T file);

    public <T extends BaseFile> T upload(InputStream file, String fileName, Class<T> clazz);

    public <T extends BaseFile> void remove(T file);

    public void download(OutputStream outputStream, String id);

    public <T extends BaseFile> void download(OutputStream outputStream, T fileInfo);

    public <T extends BaseFile> T getFileInfo(String id);

    public <T extends BaseFile> Page<T> getFileList(Pageable pageable, String filter);

    public List<String> createFile(List<Map<String, Object>> fileList);

    // 判断当前实现类支持哪种存储类型
    StorageTypeEnum getStorageType();
}
