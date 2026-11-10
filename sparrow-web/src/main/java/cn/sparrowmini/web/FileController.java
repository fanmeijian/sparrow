package cn.sparrowmini.web;

import cn.sparrowmini.file.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ContentDisposition;
import java.io.*;
import java.util.List;
@RestController @RequestMapping("/api/admin/files")
public class FileController {
    private final FileService files;
    public FileController(FileService files){this.files=files;}
    public record Rename(long version,String name) { }
    @GetMapping public List<FileService.Info> list(@RequestParam(defaultValue="0")int offset,@RequestParam(defaultValue="50")int limit,@RequestParam(required=false)String context){return files.list(offset,limit,context);}
    @PostMapping public FileService.Info upload(@RequestParam MultipartFile file,@RequestParam(defaultValue="local")String provider,@RequestParam(required=false)String context)throws IOException{
        try(var input=file.getInputStream()){return files.upload(provider,file.getOriginalFilename(),input,context);}
    }
    @GetMapping("/{id}")public FileService.Info get(@PathVariable String id,@RequestParam(required=false)String context){return files.get(id,context);}
    @GetMapping("/{id}/content")public void download(@PathVariable String id,@RequestParam(required=false)String context,HttpServletResponse response)throws IOException{
        var info=files.get(id,context);response.setContentType("application/octet-stream");response.setHeader("X-Content-Type-Options","nosniff");response.setHeader("Cache-Control","no-store");
        response.setHeader("Content-Disposition",ContentDisposition.attachment().filename(info.name(),java.nio.charset.StandardCharsets.UTF_8).build().toString());
        files.download(id,context,response.getOutputStream());
    }
    @PutMapping("/{id}")public FileService.Info rename(@PathVariable String id,@RequestBody Rename input,@RequestParam(required=false)String context){return files.rename(id,input.version(),input.name(),context);}
    @DeleteMapping("/{id}")public FileService.Info delete(@PathVariable String id,@RequestParam long version,@RequestParam(required=false)String context){return files.requestDelete(id,version,context);}
    @PostMapping("/{id}/complete-delete")public FileService.Info complete(@PathVariable String id,@RequestParam(required=false)String context)throws IOException{return files.completeDelete(id,context);}
}
