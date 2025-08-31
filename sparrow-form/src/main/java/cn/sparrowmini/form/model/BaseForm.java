package cn.sparrowmini.form.model;

import cn.sparrowmini.common.model.BaseOpLog;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.MappedSuperclass;
import java.util.Set;

@Audited
@MappedSuperclass
public abstract class BaseForm extends BaseOpLog {

    private String name;
    private String code;
    @Column(columnDefinition = "TEXT")
    private String form;

    /**
     * 查看数据列表的自定义视图
     */
    private String viewUrl;

    /**
     * 表单分类
     */
    private String catalog;

    /**
     * 需要展现的字段
     */
    @ElementCollection
    private Set<String> displayColumns;

    private FormOpenTypeEnum openType = FormOpenTypeEnum.PAGE;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public FormOpenTypeEnum getOpenType() {
        return openType;
    }

    public void setOpenType(FormOpenTypeEnum openType) {
        this.openType = openType;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public Set<String> getDisplayColumns() {
        return displayColumns;
    }

    public void setDisplayColumns(Set<String> displayColumns) {
        this.displayColumns = displayColumns;
    }

}
