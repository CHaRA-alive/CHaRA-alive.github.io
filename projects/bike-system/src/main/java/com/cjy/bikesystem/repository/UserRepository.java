package com.cjy.bikesystem.repository;

import com.cjy.bikesystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // 根据手机号查找用户
    Optional<User> findByPhone(String phone);

    // 根据信用状态查找用户
    List<User> findByCredit(String credit);

    // 根据账户状态查找用户
    List<User> findByStatus(String status);

    // 根据姓名模糊查询
    List<User> findByUserNameContaining(String userName);
}