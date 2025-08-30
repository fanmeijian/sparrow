package cn.sparrowmini.ext.file.local;

import cn.sparrowmini.common.model.BaseFile;
import cn.sparrowmini.common.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@Component
public class StorageServiceImpl implements StorageService {
    @Autowired
    private StorageServiceLocal storageServiceLocal;

    @Autowired
    private LocalFileRepository localFileRepository;

    @Override
    public <T extends BaseFile> byte[] download(T file) {
        return new byte[0];
    }

    @Override
    public <T extends BaseFile> T upload(InputStream inputStream, String fileName) {
        Map<String, Object> result = storageServiceLocal.store(inputStream);
        LocalFile localFile = new LocalFile();
        localFile.setFileName(result.get("fileName").toString());
        localFile.setName(fileName);
        localFile.setSize((Long) result.get("size"));
        localFile.setType((String) result.get("mimeType"));
        localFile.setPath((String) result.get("path"));
        localFile.setHash((String) result.get("checksum"));
        localFileRepository.save(localFile);
        return (T) localFile;
    }

    @Override
    public <T extends BaseFile> void remove(T file) {

    }

    @Override
    public void download(OutputStream outputStream, String id) {
        localFileRepository.findById(id).ifPresent(localFile -> {
            storageServiceLocal.readFile(outputStream,localFile.getPath());
        });
        throw new RuntimeException("文件步存在！" + id);
    }
}
