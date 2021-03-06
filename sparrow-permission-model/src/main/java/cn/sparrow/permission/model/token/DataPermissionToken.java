package cn.sparrow.permission.model.token;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import cn.sparrow.permission.model.resource.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Entity
@Table(name = "spr_data_permission_token")
public class DataPermissionToken implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;


  @EqualsAndHashCode.Include
  @Id
  @GenericGenerator(name = "id-generator", strategy = "uuid")
  @GeneratedValue(generator = "id-generator")
  protected String id;
  
  @JoinColumn(name = "permission_token_id")
  @OneToOne
  private SparrowPermissionToken sparrowPermissionToken;
  
  @JoinColumn(name = "model_id")
  @OneToOne
  private Model model;

  @OneToMany(mappedBy = "dataPermissionToken")
  @LazyCollection(LazyCollectionOption.FALSE)
  private List<FieldPermissionToken> fieldPermissionTokens;

}
