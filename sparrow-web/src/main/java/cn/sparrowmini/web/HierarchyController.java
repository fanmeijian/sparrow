package cn.sparrowmini.web;

import cn.sparrowmini.tree.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.math.BigDecimal;
@RestController @RequestMapping("/api/admin/trees")
public class HierarchyController {
    private final HierarchyService trees;
    public HierarchyController(HierarchyService trees){this.trees=trees;}
    public record Create(String name,HierarchyGraph.Mode mode) { }
    public record Node(long revision,String resource,String recordId,BigDecimal order) { }
    public record Edge(long revision,String parent,String child,BigDecimal order) { }
    public record Move(long revision,String oldParent,String newParent,String child,BigDecimal order) { }
    @GetMapping public List<HierarchyService.Summary> list(@RequestParam(defaultValue="0")int offset,@RequestParam(defaultValue="50")int limit){return trees.list(offset,limit);}
    @PostMapping public HierarchyService.Snapshot create(@RequestBody Create input){return trees.create(input.name(),input.mode());}
    @GetMapping("/{id}") public HierarchyService.Snapshot get(@PathVariable String id){return trees.get(id);}
    @PostMapping("/{id}/nodes") public HierarchyService.Snapshot add(@PathVariable String id,@RequestBody Node input){return trees.add(id,input.revision(),input.resource(),input.recordId(),input.order());}
    @PostMapping("/{id}/edges") public HierarchyService.Snapshot link(@PathVariable String id,@RequestBody Edge input){return trees.link(id,input.revision(),input.parent(),input.child(),input.order());}
    @DeleteMapping("/{id}/edges") public HierarchyService.Snapshot detach(@PathVariable String id,@RequestBody Edge input){return trees.detach(id,input.revision(),input.parent(),input.child());}
    @PutMapping("/{id}/move") public HierarchyService.Snapshot move(@PathVariable String id,@RequestBody Move input){return trees.move(id,input.revision(),input.oldParent(),input.newParent(),input.child(),input.order());}
    @DeleteMapping("/{id}/nodes/{node}") public HierarchyService.Snapshot deleteNode(@PathVariable String id,@PathVariable String node,@RequestParam long revision){return trees.deleteNode(id,revision,node);}
    @DeleteMapping("/{id}/subtrees/{node}") public HierarchyService.Snapshot deleteSubtree(@PathVariable String id,@PathVariable String node,@RequestParam long revision){return trees.deleteSubtree(id,revision,node);}
}
