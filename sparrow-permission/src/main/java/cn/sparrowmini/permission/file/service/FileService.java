package cn.sparrowmini.permission.file.service;

import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.common.model.BaseFile;
import cn.sparrowmini.common.service.SimpleJpaFilter;
import cn.sparrowmini.permission.file.model.SparrowFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "file")
@RequestMapping(value = "files")
public interface FileService {
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件", operationId = "upload")
    @ResponseBody
    public BaseFile upload(@RequestParam MultipartFile file);

    @GetMapping(value = "/download", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "下载", operationId = "download")
    @ResponseBody
    public byte[] download(String id);

    @GetMapping
    @ResponseBody
    public Page<SparrowFile> listFile(Pageable pageable, String filters);

    @PostMapping
    @ResponseBody
    @ResponseStatus(code = HttpStatus.CREATED)
    public ApiResponse<List<String>> saveFile(@RequestBody List<SparrowFile> files);

    @GetMapping("{fileId}")
    @ResponseBody
    public SparrowFile getFile(@PathVariable String fileId);
}
