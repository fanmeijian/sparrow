package cn.sparrow.permission.model.token;

import java.io.Serializable;
import java.util.List;

import cn.sparrow.permission.constant.PermissionExpressionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionExpression<ID> implements Serializable{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private PermissionExpressionEnum expression;
  private List<ID> ids;
}
