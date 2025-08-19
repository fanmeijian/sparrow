package cn.sparrowmini.common.model;

import cn.sparrowmini.common.model.pem.SysroleModel;
import cn.sparrowmini.common.model.pem.UserModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = TablePrefix.NAME + "model")
public class Model implements Serializable {

    public Model(String id) {
        this.id = id;
    }

    private static final long serialVersionUID = 1L;


    @Id
    @Column(length = 500)
    private String id;
    private String name;
    private String remark;

    @OneToMany(mappedBy = "model")
    private List<ModelAttribute> modelAttributes;


    @OneToMany(mappedBy = "model")
    private List<UserModel> userModels;

    @OneToMany(mappedBy = "model")
    private List<SysroleModel> sysroleModels;

}
