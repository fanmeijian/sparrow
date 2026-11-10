package cn.sparrowmini.web;

import cn.sparrowmini.system.presentation.*;
import cn.sparrowmini.security.api.CurrentActorProvider;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
public class UiPageController {
    private final UiPageService pages;private final CurrentActorProvider actors;
    public UiPageController(UiPageService pages,CurrentActorProvider actors){this.pages=pages;this.actors=actors;}
    public record Save(Long version,UiPageService.Definition definition) { }
    public record Query(String page,List<String> elements,String recordId) { }
    @GetMapping("/api/admin/ui-pages") public List<String> list(){return pages.pages();}
    @GetMapping("/api/admin/ui-pages/{page}") public UiPageService.Snapshot get(@PathVariable String page){return pages.get(page);}
    @PutMapping("/api/admin/ui-pages/{page}") public UiPageService.Snapshot save(@PathVariable String page,@RequestBody Save input){return pages.save(page,input.version(),input.definition());}
    @PostMapping("/api/ui/capabilities") public Map<String,UiCapabilityService.Decision> capabilities(@RequestBody Query input){return pages.capabilities(actors.current(),input.page(),input.elements(),input.recordId());}
}
