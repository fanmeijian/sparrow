//package cn.sparrowmini.common.service.client.listener;
//
//import cn.sparrowmini.common.CurrentUser;
//import cn.sparrowmini.common.constant.PermissionEnum;
//import cn.sparrowmini.common.exception.DenyPermissionException;
//import cn.sparrowmini.common.exception.NoPermissionException;
//import cn.sparrowmini.common.service.client.ModelPermissionServiceClient;
//import lombok.extern.slf4j.Slf4j;
//import org.hibernate.event.spi.PreDeleteEvent;
//import org.hibernate.event.spi.PreDeleteEventListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.stereotype.Component;
//
//import java.io.Serializable;
//
//@Slf4j
//@Component
//public class DeleteEventListener implements PreDeleteEventListener {
//
//    @Autowired
//    private ModelPermissionServiceClient modelPermissionServiceClient;
//
//    @Override
//    public boolean onPreDelete(PreDeleteEvent event) {
//        String modelId = event.getEntity().getClass().getName();
//        try {
//            Boolean hasPermission = modelPermissionServiceClient
//                    .hasPermission(modelId, PermissionEnum.DELETER, event.getId())
//                    .block();
//
//            if (Boolean.FALSE.equals(hasPermission)) {
//                throw new NoPermissionException("没有权限删除模型: " + modelId);
//            }
//
//            return false; // 允许删除
//        } catch (AccessDeniedException ex) {
//            throw ex; // 让 Spring Security 处理
//        } catch (Exception ex) {
//            // 包装其他异常为运行时异常
//            throw new RuntimeException("权限校验失败: " + ex.getMessage(), ex);
//        }
//    }
//
//}
