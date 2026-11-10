package cn.sparrowmini.admin;

import cn.sparrowmini.file.policy.FilePolicyService;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.IOException;
@RestController @RequestMapping("/api/files")
public class BusinessFileController {
    private final FilePolicyService files;
    public BusinessFileController(FilePolicyService files){this.files=files;}
    public record Rename(long version,String name) { }
    public record FilePage(List<Map<String,Object>> content,long totalElements,int page,int size) { }
    @GetMapping public FilePage list(@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="50")int size,@RequestParam(defaultValue="id,name,size")Set<String> fields){var result=files.list(page,size,fields);return new FilePage(result.getContent(),result.getTotalElements(),page,size);}
    @GetMapping("/{id}/content") public void download(@PathVariable String id,HttpServletResponse response)throws IOException{
        response.setContentType("application/octet-stream");response.setHeader("Cache-Control","no-store");response.setHeader("X-Content-Type-Options","nosniff");response.setHeader("Content-Disposition","attachment; filename=download");files.download(id,response.getOutputStream());
    }
    @PutMapping("/{id}") public void rename(@PathVariable String id,@RequestBody Rename input){files.rename(id,input.version(),input.name());}
    @DeleteMapping("/{id}") public void delete(@PathVariable String id,@RequestParam long version){files.requestDelete(id,version);}
}
