package cn.sparrowmini.ext.oss.txcos;

import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.common.repository.FileRepository;
import cn.sparrowmini.common.service.CommonJpaService;
import cn.sparrowmini.common.service.DownloadPermission;
import cn.sparrowmini.common.service.StorageService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.*;

@Slf4j
@RestController
@Tag(name = "cos")
@RequestMapping(value = "cos/tx")
public class TxCosController {

    @Autowired
    private TxCosConfig config;

    @Autowired
    private StorageService storageService;

    @Autowired
    private TxCosService txCosService;


    @GetMapping(value = "/uploadTmpKeys", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response getUploadTmpKey(TempKeyRequestBody body) {
        return txCosService.getUploadKey(body);
    }

//    @DownloadPermission
//    @GetMapping(value = "/downloadTmpKeys", produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    public Response getDownloadTmpKey(String fileName, String path) {
//        String[] allowActions = new String[]{
//                // 下载
//                "name/cos:GetObject"};
//
//
//        return this.getTmpkey(fileName, allowActions, path);
//    }

    @PostMapping("/create-files")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.CREATED)
    public ApiResponse<List<String>> createFile(@RequestBody List<Map<String, Object>> files) {
        return new ApiResponse<>(storageService.createFile(files));
    }

    @GetMapping("/{fileId}")
    @ResponseBody
    public TxCosFile getFile(@PathVariable String fileId) {
        return storageService.getFileInfo(fileId);
    }
//
//    @GetMapping("files")
//    @ResponseBody
//    public Page<TxCosFile> getFileList(Pageable pageable, String filter) {
//        return storageService.getFileList(pageable, filter);
//    }
//
//    @DeleteMapping
//    @ResponseBody
//    @ResponseStatus(code = HttpStatus.NO_CONTENT)
//    public void deleteFile(@RequestParam("id") Set<String> ids) {
//        fileRepository.deleteAllById(ids);
//    }


    @GetMapping("/{fileId}/download-url")
    @ResponseBody
    public ApiResponse<String> downloadUrl(@PathVariable String fileId, @RequestParam Map<String, Object> params) {
        String url = txCosService.getDownloadUrl(fileId, params);
        return new ApiResponse<>(url);
    }


}
