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
    private FileRepository fileRepository;

    @GetMapping(value = "/uploadTmpKeys", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response getUploadTmpKey(String fileName, String path) {
        String[] allowActions = new String[]{
                // 简单上传
                "name/cos:PutObject",
                // 表单上传、小程序上传
                "name/cos:PostObject",
                // 分块上传
                "name/cos:InitiateMultipartUpload", "name/cos:ListMultipartUploads", "name/cos:ListParts",
                "name/cos:UploadPart", "name/cos:CompleteMultipartUpload"};
        return this.getTmpkey(fileName, allowActions, path);
    }

    @DownloadPermission
    @GetMapping(value = "/downloadTmpKeys", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response getDownloadTmpKey(String fileName, String path) {
        String[] allowActions = new String[]{
                // 下载
                "name/cos:GetObject"};


        return this.getTmpkey(fileName, allowActions, path);
    }

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

    @GetMapping("files")
    @ResponseBody
    public Page<TxCosFile> getFileList(Pageable pageable, String filter) {
        return storageService.getFileList(pageable, filter);
    }

    @DeleteMapping
    @ResponseBody
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteFile(@RequestParam("id") Set<String> ids) {
        fileRepository.deleteAllById(ids);
    }

    @GetMapping("/{fileId}/download")
    @ResponseBody
    public byte[] download(@PathVariable String fileId) {
        TxCosFile file = storageService.getFileInfo(fileId);
        return storageService.download(file);
    }

    @GetMapping("/{fileId}/download-url")
    @ResponseBody
    public ApiResponse<String> downloadUrl(@PathVariable String fileId) {
        TxCosFile file = storageService.getFileInfo(fileId);
        // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
        // 详细代码参见本页：创建 COSClient
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = file.getBucket();
        // 对象键(Key)是对象在存储桶中的唯一标识。详情请参见 [对象键](https://cloud.tencent.com/document/product/436/13324)
        String key = String.join("",file.getPath(), file.getFileName());

        // 设置签名过期时间(可选), 若未进行设置则默认使用 ClientConfig 中的签名过期时间(1小时)
        // 这里设置签名在半个小时后过期
        Date expirationDate = new Date(System.currentTimeMillis() + 30 * 60 * 1000);

        // 填写本次请求的参数，需与实际请求相同，能够防止用户篡改此签名的 HTTP 请求的参数
        Map<String, String> params = new HashMap<String, String>();
        params.put("param1", "value1");

        // 填写本次请求的头部，需与实际请求相同，能够防止用户篡改此签名的 HTTP 请求的头部
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("header1", "value1");

        // 请求的 HTTP 方法，上传请求用 PUT，下载请求用 GET，删除请求用 DELETE
        HttpMethodName method = HttpMethodName.GET;

        URL url = cosClient.generatePresignedUrl(bucketName, key, expirationDate, method, headers, params);
        System.out.println(url.toString());

        // 确认本进程不再使用 cosClient 实例之后，关闭即可
        cosClient.shutdown();
        return new ApiResponse<>(url.toString());
    }

    private Response getTmpkey(String fileName, String[] allowActions, String path) {
        TreeMap<String, Object> config = new TreeMap<String, Object>();

        try {
            // 这里的 SecretId 和 SecretKey 代表了用于申请临时密钥的永久身份（主账号、子账号等），子账号需要具有操作存储桶的权限。
            // 替换为您的云 api 密钥 SecretId
            config.put("secretId", this.config.getSecretId());
            // 替换为您的云 api 密钥 SecretKey
            config.put("secretKey", this.config.getSecretKey());

            // 设置域名:
            // 如果您使用了腾讯云 cvm，可以设置内部域名
            // config.put("host", "sts.internal.tencentcloudapi.com");

            // 临时密钥有效时长，单位是秒，默认 1800 秒，目前主账号最长 2 小时（即 7200 秒），子账号最长 36 小时（即 129600）秒
            config.put("durationSeconds", 300);

            // 换成您的 bucket
            config.put("bucket", this.config.getBucket());// sportunione-1252583813
            // 换成 bucket 所在地区
            config.put("region", this.config.getRegion());// ap-guangzhou

            // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径
            // 列举几种典型的前缀授权场景：
            // 1、允许访问所有对象："*"
            // 2、允许访问指定的对象："a/a1.txt", "b/b1.txt"
            // 3、允许访问指定前缀的对象："a*", "a/*", "b/*"
            // 如果填写了“*”，将允许用户访问所有资源；除非业务需要，否则请按照最小权限原则授予用户相应的访问权限范围。
            config.put("allowPrefixes", this.config.getAllowPrefixes());// upload/*"

            // 密钥的权限列表。必须在这里指定本次临时密钥所需要的权限。
            // 简单上传、表单上传和分块上传需要以下的权限，其他权限列表请看
            // https://cloud.tencent.com/document/product/436/31923

            config.put("allowActions", allowActions);

            return CosStsClient.getCredential(config);
        } catch (Exception e) {
            throw new IllegalArgumentException("no valid secret !");
        }
    }

    private COSClient createCOSClient() {
        // 设置用户身份信息。
        // SECRETID 和 SECRETKEY 请登录访问管理控制台 https://console.cloud.tencent.com/cam/capi
        // 进行查看和管理
        String secretId = this.config.getSecretId();// 用户的 SecretId，建议使用子账号密钥，授权遵循最小权限指引，降低使用风险。子账号密钥获取可参见
        // https://cloud.tencent.com/document/product/598/37140
        String secretKey = this.config.getSecretKey();// 用户的 SecretKey，建议使用子账号密钥，授权遵循最小权限指引，降低使用风险。子账号密钥获取可参见
        // https://cloud.tencent.com/document/product/598/37140
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);

        // ClientConfig 中包含了后续请求 COS 的客户端设置：
        ClientConfig clientConfig = new ClientConfig();

        // 设置 bucket 的地域
        // COS_REGION 请参见 https://cloud.tencent.com/document/product/436/6224
        clientConfig.setRegion(new Region(this.config.getRegion()));

        // 设置请求协议, http 或者 https
        // 5.6.53 及更低的版本，建议设置使用 https 协议
        // 5.6.54 及更高版本，默认使用了 https
        clientConfig.setHttpProtocol(HttpProtocol.https);

        // 以下的设置，是可选的：

        // 设置 socket 读取超时，默认 30s
        clientConfig.setSocketTimeout(30 * 1000);
        // 设置建立连接超时，默认 30s
        clientConfig.setConnectionTimeout(30 * 1000);

        // 如果需要的话，设置 http 代理，ip 以及 port
//		clientConfig.setHttpProxyIp("httpProxyIp");
//		clientConfig.setHttpProxyPort(80);

        // 生成 cos 客户端。
        return new COSClient(cred, clientConfig);
    }

}
