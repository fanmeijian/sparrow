package cn.sparrowmini.web;

import cn.sparrowmini.identity.api.UserDirectoryProvider;
import cn.sparrowmini.rbac.core.*;
import cn.sparrowmini.security.api.*;
import cn.sparrowmini.datapolicy.persistence.jpa.JpaPolicyStore;
import cn.sparrowmini.group.persistence.jpa.JpaGroupService;
import cn.sparrowmini.system.dictionary.DictionaryService;
import cn.sparrowmini.file.FileService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;

/** Explicit opt-in: services retain authorization; absent modules expose no endpoints. */
@AutoConfiguration(afterName="cn.sparrowmini.identity.local.autoconfigure.LocalIdentityAutoConfiguration")
@ConditionalOnWebApplication(type=ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix="sparrow.management",name="enabled",havingValue="true")
@Import(ApiErrors.class)
public class ManagementWebConfiguration {
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnClass(UserDirectoryProvider.class)
    @ConditionalOnBean(UserDirectoryProvider.class)
    @Import(IdentityController.class)
    static class IdentityEndpoints { }
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnClass(RoleDirectoryProvider.class)
    @ConditionalOnBean({RoleDirectoryProvider.class,RoleAssignmentProvider.class,RolePermissionManager.class})
    @Import(RoleController.class)
    static class RoleEndpoints { }
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnClass(JpaPolicyStore.class)
    @ConditionalOnBean(JpaPolicyStore.class)
    @Import(PolicyController.class)
    static class PolicyEndpoints { }
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnClass(JpaGroupService.class)
    @ConditionalOnBean(JpaGroupService.class)
    @Import(GroupController.class)
    static class GroupEndpoints { }
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnClass(DictionaryService.class)
    @ConditionalOnBean(DictionaryService.class)
    @Import(DictionaryController.class)
    static class DictionaryEndpoints { }
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnClass(FileService.class)
    @ConditionalOnBean(FileService.class)
    @Import(FileController.class)
    static class FileEndpoints { }
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnClass(cn.sparrowmini.file.FileReferenceService.class)
    @ConditionalOnBean(cn.sparrowmini.file.FileReferenceService.class)
    @Import(FileReferenceController.class)
    static class FileReferenceEndpoints { }
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnClass(cn.sparrowmini.system.audit.AuditService.class)
    @ConditionalOnBean(cn.sparrowmini.system.audit.AuditService.class)
    @Import(AuditController.class)
    static class AuditEndpoints { }
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnClass(cn.sparrowmini.system.presentation.UiPageService.class)
    @ConditionalOnBean({cn.sparrowmini.system.presentation.UiPageService.class,CurrentActorProvider.class})
    @Import(UiPageController.class)
    static class UiPageEndpoints { }
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnClass(cn.sparrowmini.system.configuration.ConfigurationService.class)
    @ConditionalOnBean(cn.sparrowmini.system.configuration.ConfigurationService.class)
    @Import(ConfigurationController.class)
    static class ConfigurationEndpoints { }
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnClass(cn.sparrowmini.system.navigation.NavigationService.class)
    @ConditionalOnBean({cn.sparrowmini.system.navigation.NavigationService.class,CurrentActorProvider.class})
    @Import(NavigationController.class)
    static class NavigationEndpoints { }
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnClass(cn.sparrowmini.tree.HierarchyService.class)
    @ConditionalOnBean(cn.sparrowmini.tree.HierarchyService.class)
    @Import(HierarchyController.class)
    static class HierarchyEndpoints { }
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnBean({CurrentActorProvider.class,FunctionPermissionEvaluator.class})
    @Import(SessionController.class)
    static class SessionEndpoint { }
}
