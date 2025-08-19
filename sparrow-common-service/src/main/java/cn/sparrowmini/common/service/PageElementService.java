package cn.sparrowmini.common.service;

import cn.sparrowmini.common.UserInfo;
import cn.sparrowmini.common.constant.PermissionTypeEnum;
import cn.sparrowmini.common.model.PageElement;
import cn.sparrowmini.common.repository.PageElementRepository;
import cn.sparrowmini.common.repository.SysrolePageElementRepository;
import cn.sparrowmini.common.repository.UserPageElementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageElementService {

    @Autowired
    private PageElementRepository pageElementRepository;

    @Autowired
    private UserPageElementRepository userPageElementRepository;

    @Autowired
    private SysrolePageElementRepository sysrolePageElementRepository;

    public Map<String, PermissionTypeEnum> pageElementByPage(Collection<String> elementIds, UserInfo userInfo) {
        Map<String, PermissionTypeEnum> map = new HashMap<>();
        List<PageElement> pageElements = pageElementRepository.findByIdIn(elementIds);

        userPageElementRepository.getByElementId(elementIds,userInfo.getUsername()).forEach(userPageElement -> map.put(userPageElement.getId().getPageElementId(),userPageElement.getId().getType()));
        sysrolePageElementRepository.getByElementId(elementIds,userInfo.getRoles()).forEach(sysrolePageElement -> {
            String pageElementId = sysrolePageElement.getId().getPageElementId();
            if(!map.containsKey(pageElementId) || map.get(pageElementId).equals(PermissionTypeEnum.ALLOW)){
                map.put(pageElementId,sysrolePageElement.getId().getType());
            }
        });

        pageElements.forEach(pageElement -> {
            //配置了允许权限,但没有给当前用户授权, 则为拒绝权限
            boolean isConfigAllow = sysrolePageElementRepository.allowCount(pageElement.getId())>0 || userPageElementRepository.allowCount(pageElement.getId())>0;
            if(!map.containsKey(pageElement.getId()) && isConfigAllow){
                map.put(pageElement.getId(),PermissionTypeEnum.DENY);
            }
        });
        return map;
    }
}
