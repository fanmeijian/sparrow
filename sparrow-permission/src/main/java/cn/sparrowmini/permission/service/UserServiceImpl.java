package cn.sparrowmini.permission.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.sparrowmini.common.model.ErrMessage;
import cn.sparrowmini.permission.config.OAuthProperties;
import cn.sparrowmini.permission.repository.GroupUserRepository;
import cn.sparrowmini.permission.repository.UserRepository;
import cn.sparrowmini.permission.sysrole.repository.SysroleRepository;
import cn.sparrowmini.permission.sysrole.repository.UserSysroleRepository;
import cn.sparrowmini.permission.user.User;

@RestController
@Service
public class UserServiceImpl extends AbstractUserServiceImpl {
    @Autowired
    private Keycloak keycloak;

    @Autowired
    private OAuthProperties keycloakSpringBootProperties;

    public UserServiceImpl(UserSysroleRepository userSysroleRepository, SysroleRepository sysroleRepository, UserRepository userRepository, GroupUserRepository groupUserRepository) {
        super(userSysroleRepository, sysroleRepository, userRepository, groupUserRepository);
    }

    @Override
    public void synchronize() {
        // TODO Auto-generated method stub

    }

    @Override
    public Page<User> getAllUsers(Pageable pageable, String filter) {
        List<UserRepresentation> usersList = keycloak.realm(keycloakSpringBootProperties.getRealm()).users()
                .list((int) pageable.getOffset(), pageable.getPageSize());
        long count = keycloak.realm(keycloakSpringBootProperties.getRealm()).users().count();
        return new PageImpl<>(usersList.stream()
                .map(m -> new User(m.getUsername(), m.getEmail(), "", m.getId(), m.getFirstName(), m.getLastName(),m.isEnabled()))
                .collect(Collectors.toList()), pageable, count);
    }

    @Override
    public Map<String, List<ErrMessage>> create(Set<User> users) {
        users.forEach(u -> {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue("keycloakPassword");
            credential.setTemporary(true);

            UserRepresentation userRepresentation = new UserRepresentation();
            userRepresentation.setUsername(u.getUsername());
            userRepresentation.setFirstName(u.getFirstName());
            userRepresentation.setLastName(u.getLastName());
            userRepresentation.setCredentials(List.of(credential));
            userRepresentation.setEnabled(true);

            Response result = null;
            try {
                result = keycloak.realm(keycloakSpringBootProperties.getRealm()).users().create(userRepresentation);
            } catch (Exception e) {
                System.out.println(e);
            }

            if (result == null || result.getStatus() != 201) {
                assert result != null;
                System.err.println("Couldn't create Keycloak user." + result.getStatus());
            } else {
                System.out.println("Keycloak user created.... verify in keycloak!");
            }
        });


        return new HashMap<>();
    }

    @Override
    public void resetPassword(@PathVariable String username, @RequestBody String password){
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(true);

        UsersResource usersResource = keycloak.realm(keycloakSpringBootProperties.getRealm()).users();
        UserRepresentation userRepresentation = usersResource.search(username,true).get(0);
        UserResource userResource = usersResource.get(userRepresentation.getId());
        userResource.resetPassword(credential);

    }

    @Override
    public void enable(@PathVariable String username, @RequestParam Boolean enabled){
        UsersResource usersResource = keycloak.realm(keycloakSpringBootProperties.getRealm()).users();
        UserRepresentation userRepresentation = usersResource.search(username,true).get(0);
        UserResource userResource = usersResource.get(userRepresentation.getId());
        userRepresentation.setEnabled(enabled);
        userResource.update(userRepresentation);
    }

    @Override
    public List<ErrMessage> update(@PathVariable String username, @RequestBody Map<String, Object> map) {
        UsersResource usersResource = keycloak.realm(keycloakSpringBootProperties.getRealm()).users();
        UserRepresentation userRepresentation = usersResource.search(username,true).get(0);
        UserResource userResource = usersResource.get(userRepresentation.getId());

        map.forEach((key, value) -> {
            switch (key){
                case "firstName":
                    userRepresentation.setFirstName(map.get(key).toString());
                    break;
                case "lastName":
                    userRepresentation.setLastName(map.get(key).toString());
                    break;
                case "email":
                    userRepresentation.setEmail(map.get(key).toString());
                    break;
                default:
                    break;
            }
        });
        userResource.update(userRepresentation);
        return new ArrayList<>();
    }

}
