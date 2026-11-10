package cn.sparrowmini.web;

import cn.sparrowmini.system.navigation.NavigationService;
import cn.sparrowmini.security.api.CurrentActorProvider;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
public class NavigationController {
    private final NavigationService navigation;private final CurrentActorProvider actors;
    public NavigationController(NavigationService navigation,CurrentActorProvider actors){this.navigation=navigation;this.actors=actors;}
    @GetMapping("/api/admin/navigation") public NavigationService.Snapshot get(){return navigation.get();}
    @PutMapping("/api/admin/navigation") public NavigationService.Snapshot save(@RequestBody NavigationService.Snapshot input){return navigation.save(input.version(),input.items());}
    @GetMapping("/api/navigation") public List<NavigationService.VisibleItem> visible(){return navigation.visible(actors.current());}
}
