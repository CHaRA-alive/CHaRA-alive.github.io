package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.User;
import com.cjy.bikesystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 1. 获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. 根据ID获取用户
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    // 3. 添加用户
    public User addUser(User user) {
        return userRepository.save(user);
    }

    // 4. 更新用户
    public User updateUser(String userId, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUserName(userDetails.getUserName());
            user.setPhone(userDetails.getPhone());
            user.setGender(userDetails.getGender());
            user.setAge(userDetails.getAge());
            user.setStatus(userDetails.getStatus());
            user.setCredit(userDetails.getCredit());
            user.setBalance(userDetails.getBalance());
            return userRepository.save(user);
        }
        return null;
    }

    // 5. 删除用户
    public boolean deleteUser(String userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    // 6. 根据手机号查找用户
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    // 7. 根据信用状态查询用户
    public List<User> getUsersByCredit(String credit) {
        return userRepository.findByCredit(credit);
    }

    // 8. 根据状态查询用户
    public List<User> getUsersByStatus(String status) {
        return userRepository.findByStatus(status);
    }

    // 9. 根据姓名模糊查询
    public List<User> searchUsersByName(String name) {
        return userRepository.findByUserNameContaining(name);
    }

    // 10. 充值余额
    public boolean rechargeBalance(String userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            BigDecimal balance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
            user.setBalance(balance.add(amount));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    // 11. 更新信用状态
    public boolean updateCredit(String userId, String credit) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setCredit(credit);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
