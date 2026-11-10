package cn.sparrowmini.web;

import cn.sparrowmini.system.configuration.ConfigurationService;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
public class ConfigurationController {
    private final ConfigurationService configurations;
    public ConfigurationController(ConfigurationService configurations){this.configurations=configurations;}
    public record Save(Long version,String value) { }
    @GetMapping("/api/admin/configurations") public List<ConfigurationService.Value> list(){return configurations.list();}
    @PutMapping("/api/admin/configurations/{code}") public ConfigurationService.Value save(@PathVariable String code,@RequestBody Save input){return configurations.save(code,input.version(),input.value());}
    @DeleteMapping("/api/admin/configurations/{code}") public ConfigurationService.Value reset(@PathVariable String code,@RequestParam long version){return configurations.reset(code,version);}
    @GetMapping("/api/configuration") public Map<String,String> clientValues(){return configurations.clientValues();}
}
