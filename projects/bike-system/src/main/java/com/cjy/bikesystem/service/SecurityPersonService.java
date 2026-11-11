package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.SecurityPerson;
import com.cjy.bikesystem.repository.SecurityPersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SecurityPersonService {

    @Autowired
    private SecurityPersonRepository securityPersonRepository;

    // 1. 获取所有安保人员
    public List<SecurityPerson> getAllSecurityPersons() {
        return securityPersonRepository.findAll();
    }

    // 2. 根据ID获取安保人员
    public Optional<SecurityPerson> getSecurityPersonById(String securityId) {
        return securityPersonRepository.findById(securityId);
    }

    // 3. 添加安保人员
    public SecurityPerson addSecurityPerson(SecurityPerson person) {
        return securityPersonRepository.save(person);
    }

    // 4. 更新安保人员
    public SecurityPerson updateSecurityPerson(String securityId, SecurityPerson personDetails) {
        var person = securityPersonRepository.findById(securityId);
        if (person.isPresent()) {
            SecurityPerson securityPerson = person.get();
            securityPerson.setSecName(personDetails.getSecName());
            securityPerson.setSecPhone(personDetails.getSecPhone());
            securityPerson.setArea(personDetails.getArea());
            return securityPersonRepository.save(securityPerson);
        }
        return null;
    }

    // 5. 删除安保人员
    public boolean deleteSecurityPerson(String securityId) {
        if (securityPersonRepository.existsById(securityId)) {
            securityPersonRepository.deleteById(securityId);
            return true;
        }
        return false;
    }

    // 6. 根据手机号查找安保人员
    public SecurityPerson getSecurityPersonByPhone(String phone) {
        return securityPersonRepository.findBySecPhone(phone);
    }
}