package cn.sparrowmini.common.rest;

import cn.sparrowmini.common.model.BaseFile;
import cn.sparrowmini.common.service.StorageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RestController
@RequestMapping("files")
public class FileController {
    @Autowired(required = false)
    private StorageService storageService;

    @GetMapping(value = "/download/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public <T extends BaseFile> ResponseEntity<Void> download(@PathVariable String id, HttpServletResponse response) {
        T fileInfo = storageService.getFileInfo(id);
        // 设置响应头信息
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileInfo.getName() + "\"");
        response.setContentLengthLong(fileInfo.getSize());
        try(OutputStream outputStream = response.getOutputStream()){
            storageService.download(outputStream, id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public <T extends BaseFile> T upload(@RequestParam("file") MultipartFile file) {
        try(InputStream inputStream = file.getInputStream()){
            return storageService.upload(inputStream, file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
