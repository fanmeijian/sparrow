package cn.sparrowmini.web;

import cn.sparrowmini.group.persistence.jpa.JpaGroupService;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/groups")
public class GroupController {
    private final JpaGroupService groups;
    public GroupController(JpaGroupService groups){this.groups=groups;}
    public record Create(long revision,String code,String name) { }
    public record Member(long revision,String group,String subject) { }
    public record Edge(long revision,String parent,String child) { }
    public record State(long revision,String group,boolean enabled) { }
    @GetMapping public JpaGroupService.Snapshot get(){return groups.inspect();}
    @PostMapping public JpaGroupService.Snapshot create(@RequestBody Create input){return groups.create(input.revision(),input.code(),input.name());}
    @PostMapping("/members")public JpaGroupService.Snapshot add(@RequestBody Member input){return groups.addMember(input.revision(),input.group(),input.subject());}
    @DeleteMapping("/members")public JpaGroupService.Snapshot remove(@RequestBody Member input){return groups.removeMember(input.revision(),input.group(),input.subject());}
    @PostMapping("/edges")public JpaGroupService.Snapshot addEdge(@RequestBody Edge input){return groups.addChild(input.revision(),input.parent(),input.child());}
    @DeleteMapping("/edges")public JpaGroupService.Snapshot removeEdge(@RequestBody Edge input){return groups.removeChild(input.revision(),input.parent(),input.child());}
    @PutMapping("/state")public JpaGroupService.Snapshot state(@RequestBody State input){return groups.setEnabled(input.revision(),input.group(),input.enabled());}
    @DeleteMapping("/{id}")public JpaGroupService.Snapshot delete(@PathVariable String id,@RequestParam long revision){return groups.delete(revision,id);}
}
