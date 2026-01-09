package cn.sparrowmini.common.zk.view;

import cn.sparrowmini.common.UserInfo;
import cn.sparrowmini.common.model.Menu;
import cn.sparrowmini.common.repository.MenuRepository;
import cn.sparrowmini.common.service.MenuService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zkplus.spring.SpringUtil;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Component
@Scope("prototype")
@VariableResolver(DelegatingVariableResolver.class)
public class UserVM {
    List<Menu> userInfoList = new ArrayList<>();

    @WireVariable
    private MenuRepository menuRepository;


    @Init
    public void init(){
        log.info("init user {}",menuRepository);
        this.userInfoList= menuRepository.findAll();
    }
}
