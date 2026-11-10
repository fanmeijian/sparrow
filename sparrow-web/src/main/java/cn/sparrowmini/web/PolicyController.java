package cn.sparrowmini.web;

import cn.sparrowmini.datapolicy.persistence.jpa.*;
import cn.sparrowmini.security.api.PermissionRequest;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/policies")
public class PolicyController {
    private final JpaPolicyStore store;private final PolicyJsonCodec codec=new PolicyJsonCodec();
    public PolicyController(JpaPolicyStore store){this.store=store;}
    public record Draft(Long version,String document) { }
    public record Version(long version) { }
    public record View(Long version,long activeRevision,String document) { }
    @GetMapping public View get(@RequestParam String resource,@RequestParam String scope){try{return view(store.draft(new PermissionRequest(resource,scope)));}catch(java.util.NoSuchElementException missing){return new View(null,0,"[]");}}
    @PutMapping public View draft(@RequestParam String resource,@RequestParam String scope,@RequestBody Draft input){return view(store.saveDraft(new PermissionRequest(resource,scope),input.version(),codec.decode(input.document())));}
    @PostMapping("/publish")public View publish(@RequestParam String resource,@RequestParam String scope,@RequestBody Version input){return view(store.publish(new PermissionRequest(resource,scope),input.version()));}
    @PostMapping("/retire")public void retire(@RequestParam String resource,@RequestParam String scope,@RequestBody Version input){store.retire(new PermissionRequest(resource,scope),input.version());}
    private View view(JpaPolicyStore.Snapshot snapshot){return new View(snapshot.version(),snapshot.activeRevision(),codec.encode(snapshot.rules()));}
}
