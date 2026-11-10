package cn.sparrowmini.web;

import cn.sparrowmini.system.dictionary.DictionaryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/admin/dictionaries")
public class DictionaryController {
    private final DictionaryService dictionaries;
    public DictionaryController(DictionaryService dictionaries){this.dictionaries=dictionaries;}
    public record Create(String code,String name) { }
    public record Entry(String code,String label,int order) { }
    public record Update(long version,String label,int order,boolean enabled) { }
    @GetMapping public List<DictionaryService.Definition> list(@RequestParam(defaultValue="0")int offset,@RequestParam(defaultValue="50")int limit){return dictionaries.definitions(offset,limit);}
    @PostMapping public DictionaryService.Definition create(@RequestBody Create input){return dictionaries.create(input.code(),input.name());}
    @GetMapping("/{code}/entries")public List<DictionaryService.Entry> entries(@PathVariable String code,@RequestParam(defaultValue="false")boolean selectable,@RequestParam(defaultValue="0")int offset,@RequestParam(defaultValue="50")int limit){return dictionaries.entries(code,selectable,offset,limit);}
    @PostMapping("/{code}/entries")public DictionaryService.Entry add(@PathVariable String code,@RequestBody Entry input){return dictionaries.add(code,input.code(),input.label(),input.order());}
    @PutMapping("/{code}/entries/{id}")public DictionaryService.Entry update(@PathVariable String code,@PathVariable String id,@RequestBody Update input){return dictionaries.update(code,id,input.version(),input.label(),input.order(),input.enabled());}
    @DeleteMapping("/{code}/entries/{id}")public void delete(@PathVariable String code,@PathVariable String id,@RequestParam long version){dictionaries.deleteEntry(code,id,version);}
}
