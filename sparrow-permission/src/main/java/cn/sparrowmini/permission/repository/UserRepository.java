package cn.sparrowmini.permission.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import cn.sparrowmini.permission.user.User;

public interface UserRepository extends JpaRepository<User, String> {

}
