package cn.sparrowmini.common.rest;

import cn.sparrowmini.common.model.AppConfig;
import cn.sparrowmini.common.model.AppConfigAttachment;
import cn.sparrowmini.common.repository.AppConfigAttachmentRepository;
import cn.sparrowmini.common.repository.AppConfigRepository;
import cn.sparrowmini.common.view.AppConfigInfo;
import cn.sparrowmini.common.view.AppConfigView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("app-configs")
public class AppConfigController {

    @Autowired
    private AppConfigRepository appConfigRepository;

    @Autowired
    private AppConfigAttachmentRepository appConfigAttachmentRepository;

    @GetMapping("")
    @ResponseBody
    public Page<AppConfigView> getAppConfigList(Pageable pageable, String filter){
        return appConfigRepository.findAllProjection(pageable, filter, AppConfigView.class);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public AppConfig getAppConfig(@PathVariable String id){
        return appConfigRepository.findById(id).orElseThrow();
    }

    @GetMapping("/attachments/{attachmentId}")
    @ResponseBody
    public AppConfigAttachment getAppConfigAttachment(@PathVariable String attachmentId){
        return appConfigAttachmentRepository.findById(attachmentId).orElseThrow();
    }

    @DeleteMapping("/attachments")
    @ResponseBody
    public void deleteAppConfigAttachment(@RequestParam("id") Set<String> attachmentIds){
        appConfigAttachmentRepository.deleteByIds(attachmentIds);
    }

    @PostMapping
    @ResponseBody
    public void saveAppConfig(@RequestBody Map<String, Object> appConfig){
        appConfigRepository.upsert(List.of(appConfig));
    }

}
