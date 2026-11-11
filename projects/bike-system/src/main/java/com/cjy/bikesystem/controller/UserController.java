package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.User;
import com.cjy.bikesystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    // 获取所有用户
    @GetMapping("/users")
    public List<User> getAllUsers() {
        System.out.println("获取所有用户");
        return userService.getAllUsers();
    }

    // 根据ID获取用户
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        System.out.println("获取用户ID: " + userId);
        Optional<User> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 创建用户
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        System.out.println("创建用户: " + user.getUserName());
        return userService.addUser(user);
    }

    // 更新用户
    @PutMapping("/users/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable String userId, @RequestBody User userDetails) {
        System.out.println("更新用户ID: " + userId);
        User updatedUser = userService.updateUser(userId, userDetails);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    // 删除用户
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable String userId) {
        System.out.println("删除用户ID: " + userId);
        boolean deleted = userService.deleteUser(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", deleted);
        return deleted ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // 根据手机号查找用户
    @GetMapping("/users/phone/{phone}")
    public ResponseEntity<User> getUserByPhone(@PathVariable String phone) {
        System.out.println("根据手机号查找: " + phone);
        Optional<User> user = userService.getUserByPhone(phone);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 根据信用状态查询用户
    @GetMapping("/users/credit/{credit}")
    public List<User> getUsersByCredit(@PathVariable String credit) {
        System.out.println("根据信用状态查询: " + credit);
        return userService.getUsersByCredit(credit);
    }

    // 根据状态查询用户
    @GetMapping("/users/status/{status}")
    public List<User> getUsersByStatus(@PathVariable String status) {
        System.out.println("根据状态查询: " + status);
        return userService.getUsersByStatus(status);
    }

    // 根据姓名搜索用户
    @GetMapping("/users/search")
    public List<User> searchUsersByName(@RequestParam String name) {
        System.out.println("搜索用户姓名: " + name);
        return userService.searchUsersByName(name);
    }

    // 充值余额
    @PostMapping("/users/{userId}/recharge")
    public ResponseEntity<Map<String, Boolean>> rechargeBalance(
            @PathVariable String userId,
            @RequestParam BigDecimal amount) {
        System.out.println("用户充值: " + userId + ", 金额: " + amount);
        boolean success = userService.rechargeBalance(userId, amount);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);
        return success ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // 更新信用状态
    @PutMapping("/users/{userId}/credit")
    public ResponseEntity<Map<String, Boolean>> updateCredit(
            @PathVariable String userId,
            @RequestParam String credit) {
        System.out.println("更新用户信用: " + userId + ", 信用: " + credit);
        boolean success = userService.updateCredit(userId, credit);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);
        return success ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }
}