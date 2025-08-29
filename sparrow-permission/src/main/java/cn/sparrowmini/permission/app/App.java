package cn.sparrowmini.permission.app;

import cn.sparrowmini.common.model.BaseUuidEntity;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * 用于全局控制整个sparrow系统的管理。例如应用的基本路劲和访问配置等，为PORTAL做准备
 */

@Entity
@Table(name = TablePrefix.NAME + "app")
@Getter
@Setter
@NoArgsConstructor
public class App extends BaseUuidEntity {

    private String code;
    private String name;
}
