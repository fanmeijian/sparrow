package cn.sparrowmini.web;

import cn.sparrowmini.file.FileReferenceService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/admin/files/{file}/references")
public class FileReferenceController {
    private final FileReferenceService references;
    public FileReferenceController(FileReferenceService references){this.references=references;}
    public record Attach(String resource,String recordId,String slot) { }
    @GetMapping public List<FileReferenceService.Reference> list(@PathVariable String file){return references.list(file);}
    @PostMapping public FileReferenceService.Reference attach(@PathVariable String file,@RequestBody Attach input){return references.attach(file,input.resource(),input.recordId(),input.slot());}
    @DeleteMapping("/{id}") public void detach(@PathVariable String file,@PathVariable String id,@RequestParam long version){references.detach(file,id,version);}
}
