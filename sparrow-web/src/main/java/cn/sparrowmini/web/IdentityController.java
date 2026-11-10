package cn.sparrowmini.web;

import cn.sparrowmini.identity.api.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/admin/users")
public class IdentityController {
    private final UserDirectoryProvider provider;
    public IdentityController(UserDirectoryProvider provider){this.provider=provider;}
    public record Create(String username,String displayName,String password) { }
    public record Update(long version,String displayName) { }
    public record Enabled(long version,boolean enabled) { }
    public record Password(long version,String password) { }
    @GetMapping("/capabilities") public java.util.Set<IdentityCapability> capabilities(){return provider.capabilities();}
    @GetMapping public List<UserProfile> list(@RequestParam(defaultValue="")String prefix,@RequestParam(defaultValue="0")int offset,@RequestParam(defaultValue="50")int limit){return provider.list(prefix,offset,limit);}
    @GetMapping("/{id}") public UserProfile get(@PathVariable String id){return provider.get(id);}
    @PostMapping public UserProfile create(@RequestBody Create input){return writable(IdentityCapability.CREATE).create(input.username(),input.displayName(),input.password());}
    @PutMapping("/{id}") public UserProfile update(@PathVariable String id,@RequestBody Update input){return writable(IdentityCapability.UPDATE).update(id,input.version(),input.displayName());}
    @PutMapping("/{id}/enabled") public UserProfile enabled(@PathVariable String id,@RequestBody Enabled input){return writable(IdentityCapability.SET_ENABLED).setEnabled(id,input.version(),input.enabled());}
    @PutMapping("/{id}/password") public void password(@PathVariable String id,@RequestBody Password input){writable(IdentityCapability.RESET_PASSWORD).resetPassword(id,input.version(),input.password());}
    private UserLifecycleProvider writable(IdentityCapability capability){if(provider.capabilities().contains(capability) && provider instanceof UserLifecycleProvider lifecycle)return lifecycle;throw new UnsupportedOperationException("Read-only identity provider");}
}
