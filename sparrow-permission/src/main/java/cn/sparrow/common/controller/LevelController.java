package cn.sparrow.common.controller;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.sun.istack.NotNull;
import cn.sparrow.model.common.MyTree;
import cn.sparrow.model.organization.Level;
import cn.sparrow.model.organization.LevelRelationPK;
import cn.sparrow.organization.service.LevelService;

@Service
public class LevelController {

  @Autowired LevelService levelService;
  
  @PostMapping("/levels/relations")
  public void addRelations(@NotNull @RequestBody Set<LevelRelationPK> ids) {
    levelService.addRelations(ids);
  }
  
  @DeleteMapping("/levels/relations")
  public void delRelations(@NotNull @RequestBody Set<LevelRelationPK> ids) {
    levelService.delRelations(ids);
  }
  
  
  @GetMapping("/roles/getTreeByParentId")
  public MyTree<Level> tree(@Nullable @RequestParam("parentId") String parentId){
    return levelService.getTree(parentId);
  }
}