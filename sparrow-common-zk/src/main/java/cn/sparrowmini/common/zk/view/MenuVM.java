package cn.sparrowmini.common.zk.view;

import cn.sparrowmini.common.model.Menu;
import cn.sparrowmini.common.repository.MenuRepository;
import cn.sparrowmini.common.util.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.ext.TreeSelectableModel;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Scope("prototype")
@VariableResolver(DelegatingVariableResolver.class)
public class MenuVM extends AbstractTreeModel<Menu> implements TreeSelectableModel {

    @WireVariable
    private MenuRepository menuRepository;

    // 根菜单
    private List<Menu> _tree = new ArrayList<>();

    // 控制折叠状态
    private boolean collapsed = false;

    public MenuVM() {
        super(new Menu()); // 根节点 Menu，可以是空的
    }


    @Init
    public void init() {
        _tree = getChildren(null);
    }

    // 根据父菜单获取子菜单
    public List<Menu> getChildren(String parentId) {
        return parentId ==null
                ?menuRepository.findAll(menuRepository.filterSpecification("parentId is null"))
                : menuRepository.findByParentId(parentId, PageRequest.of(0,Integer.MAX_VALUE)).getContent();
    }


    /**
     * @Wire
     *     private Include contentInclude;
     *
     *     @Override
     *     public void doAfterCompose(Window comp) throws Exception {
     *         super.doAfterCompose(comp);
     *
     *         EventQueue<Event> queue =
     *                 EventQueues.lookup("NAV_QUEUE", EventQueues.APPLICATION, true);
     *
     *         queue.subscribe(event -> {
     *             if ("onNavigate".equals(event.getName())) {
     *                 Menu menu = (Menu) event.getData();
     *                 contentInclude.setSrc(menu.getZul());
     *             }
     *         });
     *     }
     * @param menu
     */
    @Command
    public void selectMenu(@BindingParam("menu") Menu menu) {
        if (menu == null || menu.getUrl() == null) {
            return;
        }

        // 发布事件
        EventQueue<Event> queue =
                EventQueues.lookup("NAV_QUEUE", EventQueues.APPLICATION, true);

        queue.publish(new Event("onNavigate", null, menu));
    }

    @Command
    public void toggleCollapse() {
        collapsed = !collapsed;
    }

    @Override
    public boolean isLeaf(Menu menu) {
        return (getChildCount(menu) == 0);
    }

    @Override
    public Menu getChild(Menu menu, int i) {
        List<Menu> menus = getChildren(menu.getId());
        if (i >= menus.size())
            return null;
        else
            return menus.get(i);
    }

    @Override
    public int getChildCount(Menu menu) {
        List<Menu> menus = getChildren(menu.getId());
        return menus.size();
    }


}