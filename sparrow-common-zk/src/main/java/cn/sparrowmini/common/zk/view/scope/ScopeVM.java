package cn.sparrowmini.common.zk.view.scope;

import cn.sparrowmini.common.repository.ScopeRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.*;
import org.zkoss.formbuilder.FormField;
import org.zkoss.formbuilder.FormModel;
import org.zkoss.formbuilder.FormNode;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.impl.InputElement;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
@Component("scopeVM")
@Scope("session")
@VariableResolver(DelegatingVariableResolver.class)
public class ScopeVM {
    private FormModel formModel;

    @Setter
    private cn.sparrowmini.common.model.Scope scope;

    List<cn.sparrowmini.common.model.Scope> scopeList = new ArrayList<>();

    @WireVariable
    private ScopeRepository scopeRepository;

    /* ===== 查询条件 ===== */
    private String nameKeyword;
    private String codeKeyword;

    /* ===== 分页参数 ===== */
    private int pageSize = 10;
    private int activePage = 0;
    private long totalSize;


    @Init
    public void init(){
        loadData();
        this.scope=new cn.sparrowmini.common.model.Scope();
        System.out.println(this);
    }

    @GlobalCommand("refreshList")
    @NotifyChange({"scopeList", "totalSize", "scope"})
    public void refreshList() {
//        loadData(); // 重新查询数据库，保证 List 是最新的
        log.info("UI 已同步：列表刷新完成");
    }


    /* ========== 查询 ========== */
    @Command
    @NotifyChange({"scopeList", "totalSize", "activePage"})
    public void search() {
        activePage = 0;
        loadData();
    }

    @Command
    @NotifyChange({"scopeList","scope","totalSize"})
    public void save() {
        System.out.println(this.scopeRepository.save(scope));
        this.onPaging(this.activePage);
        BindUtils.postGlobalCommand(null, null, "refreshList", null);
    }

    @Command
    @NotifyChange({"scopeList"})
    public void onPaging(@BindingParam("page") int page) {
        this.activePage = page;
        loadData();
    }

    private void loadData() {
        Pageable pageable = PageRequest.of(activePage, pageSize);

        Specification<cn.sparrowmini.common.model.Scope> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(nameKeyword)) {
                predicates.add(
                        cb.like(root.get("name"), "%" + nameKeyword + "%")
                );
            }

            if (StringUtils.hasText(codeKeyword)) {
                predicates.add(
                        cb.like(root.get("code"), "%" + codeKeyword + "%")
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<cn.sparrowmini.common.model.Scope> page = scopeRepository.findAll(spec, pageable);

        scopeList = page.getContent();
        totalSize = page.getTotalElements();
    }

    /* ========== 删除 ========== */
    @Command
    @NotifyChange({"scopeList", "totalSize"})
    public void remove(@BindingParam("item") cn.sparrowmini.common.model.Scope scope) {
        scopeRepository.delete(scope);
        loadData();
    }
}
