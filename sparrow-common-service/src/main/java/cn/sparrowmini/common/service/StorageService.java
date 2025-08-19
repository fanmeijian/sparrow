package cn.sparrowmini.common.service;

import cn.sparrowmini.common.model.BaseFile;
import org.springframework.data.domain.Page;

import java.io.InputStream;
import java.util.Map;

public interface StorageService {
    public byte[] download(BaseFile baseFile);

    public BaseFile upload(InputStream file, String fileName);

    public void remove(BaseFile baseFile);

}
