package cn.sparrow.common.controller;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cn.sparrow.model.common.PermissionEnum;
import cn.sparrow.model.permission.PermissionToken;
import cn.sparrow.model.permission.SparrowPermissionToken;
import cn.sparrow.permission.service.PermissionService;
import cn.sparrow.permission.service.PermissionTokenService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
public class PermissionTokenController {
  @Autowired
  PermissionTokenService permissionTokenService;
  
  @Autowired
  PermissionService<PermissionToken> permissionService;

  @GetMapping("/permissionTokens/{tokenId}")
  public SparrowPermissionToken get(@NotBlank @RequestParam("tokenId") final String tokenId) {
    return permissionTokenService.buildToken(tokenId);
  }

  @PostMapping("/permissionTokens")
  public SparrowPermissionToken create(
      @NotNull @RequestBody final PermissionToken permissionToken) {
    return permissionTokenService.save(permissionToken);
  }

  @GetMapping("/permissionTokens/hasPermission")
  public boolean hasPermission(@RequestParam("tokenId") final String tokenId,
      @RequestParam("username") String username) {
    return permissionService.hasPermission(username, tokenId, PermissionEnum.READER);
  }
}