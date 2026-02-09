package cn.sparrowmini.ext.oss.txcos;

import cn.sparrowmini.common.constant.StorageTypeEnum;
import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.common.model.BaseFile;
import cn.sparrowmini.common.repository.FileRepository;
import cn.sparrowmini.common.service.CommonJpaService;
import cn.sparrowmini.common.service.StorageService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.utils.IOUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TxCosStorageService implements StorageService {

    @Autowired
    private TxCosService txCosService;

    @Autowired
    private TxCosConfig config;

    @Autowired
    private TxFileRepository txFileRepository;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private HttpServletResponse response;
    @Override
    public <T extends BaseFile> byte[] download(T file_) {
        final TxCosFile file = (TxCosFile)file_;
        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = file.getBucket();
        // 对象键(Key)是对象在存储桶中的唯一标识。详情请参见
        // [对象键](https://cloud.tencent.com/document/product/436/13324)
        String key = String.join("",file.getPath(), file.getFileName());

        String region = file.getRegion();
        COSClient cosClient = txCosService.createCOSClient(region);

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        InputStream cosObjectInput = null;

        try {
            COSObject cosObject = cosClient.getObject(getObjectRequest);
            cosObjectInput = cosObject.getObjectContent();
        } catch (CosClientException e) {
            log.error(e.getMessage(), e);
        }

        // 处理下载到的流
        // 这里是直接读取，按实际情况来处理
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(cosObjectInput);
            // 在流没有处理完之前，不能关闭 cosClient
            // 确认本进程不再使用 cosClient 实例之后，关闭即可
            cosClient.shutdown();

            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"");

            return bytes;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            // 用完流之后一定要调用 close()
            try {
                cosObjectInput.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        // 在流没有处理完之前，不能关闭 cosClient
        // 确认本进程不再使用 cosClient 实例之后，关闭即可
        cosClient.shutdown();
        return bytes;
    }

    @Override
    public < T extends BaseFile> T upload(InputStream inputStream, String fileName, Class<T> clazz) {
        throw new RuntimeException("请使用临时密钥客户端上传！");
    }

    @Override
    public void remove(BaseFile file) {
        throw new RuntimeException("暂不支持");
    }



    @Override
    public void download(OutputStream outputStream, String id) {
        throw new RuntimeException("请使用预签名客户端下载");
    }

    @Override
    public <T extends BaseFile> void download(OutputStream outputStream, T fileInfo) {
        throw new RuntimeException("请使用预签名客户端下载");
    }

    @Override
    public <T extends BaseFile> T getFileInfo(String id) {
        return (T) txFileRepository.findById(id).orElseThrow();
    }

    @Override
    public <T extends BaseFile> Page<T> getFileList(Pageable pageable, String filter) {
        return (Page<T>) txFileRepository.findAll(pageable,filter);
    }

    @Transactional
    @Override
    public List<String> createFile(List<Map<String, Object>> fileList) {
        return txFileRepository.upsert(fileList);
    }

    @Override
    public StorageTypeEnum getStorageType() {
        return StorageTypeEnum.TX_COS;
    }

}
