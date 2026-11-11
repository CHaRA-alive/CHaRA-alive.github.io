package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.User;
import com.cjy.bikesystem.service.UserService;
import com.cjy.bikesystem.web.dto.RechargeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable String userId, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(userId, userDetails);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable String userId) {
        boolean deleted = userService.deleteUser(userId);
        return deleted
                ? ResponseEntity.ok(Map.of("deleted", true))
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/users/phone/{phone}")
    public ResponseEntity<User> getUserByPhone(@PathVariable String phone) {
        return userService.getUserByPhone(phone)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/credit/{credit}")
    public List<User> getUsersByCredit(@PathVariable String credit) {
        return userService.getUsersByCredit(credit);
    }

    @GetMapping("/users/status/{status}")
    public List<User> getUsersByStatus(@PathVariable String status) {
        return userService.getUsersByStatus(status);
    }

    @GetMapping("/users/search")
    public List<User> searchUsersByName(@RequestParam String name) {
        return userService.searchUsersByName(name);
    }

    @PostMapping("/users/{userId}/recharge")
    public ResponseEntity<Map<String, Boolean>> rechargeBalance(
            @PathVariable String userId,
            @RequestBody(required = false) RechargeRequest request,
            @RequestParam(required = false) BigDecimal amount
    ) {
        BigDecimal resolvedAmount = request == null ? amount : request.amount();
        boolean success = userService.rechargeBalance(userId, resolvedAmount);
        Map<String, Boolean> response = new LinkedHashMap<>();
        response.put("success", success);
        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/users/{userId}/credit")
    public ResponseEntity<Map<String, Boolean>> updateCredit(
            @PathVariable String userId,
            @RequestParam String credit
    ) {
        boolean success = userService.updateCredit(userId, credit);
        Map<String, Boolean> response = new LinkedHashMap<>();
        response.put("success", success);
        return success ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }
}
