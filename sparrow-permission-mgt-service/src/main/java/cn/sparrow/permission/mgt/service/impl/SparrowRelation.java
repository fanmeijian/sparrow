package cn.sparrow.permission.mgt.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SparrowRelation<T, ID> {
  private ID id;
  private ID parentId;
}
