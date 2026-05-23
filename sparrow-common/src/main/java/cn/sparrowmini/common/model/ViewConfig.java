package cn.sparrowmini.common.model;

import cn.sparrowmini.common.model.BaseUuidEntity;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = TablePrefix.NAME + "view_config")
public class ViewConfig extends BaseUuidEntity {
    private String label;
    private String code;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ViewColumn> json= new ArrayList<>();
}
