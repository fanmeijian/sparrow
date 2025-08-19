package cn.sparrowmini.common.model;

import cn.sparrowmini.common.model.pem.SysrolePageElement;
import cn.sparrowmini.common.model.pem.UserPageElement;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TablePrefix.NAME + "page_element")
public class PageElement extends BaseEntity {

    @Id
    private String id;
    private String name;
    private String remark;

    /**
     * 表明这个元素是属于哪个页面
     */
    private String pageId;

    @JsonManagedReference
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "pageElement")
    private List<UserPageElement> userPageElements;

    @JsonManagedReference
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "pageElement")
    private Set<SysrolePageElement> sysrolePageElements;
}
