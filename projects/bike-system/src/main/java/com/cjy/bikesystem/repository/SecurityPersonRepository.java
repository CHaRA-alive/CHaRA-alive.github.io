package com.cjy.bikesystem.repository;

import com.cjy.bikesystem.entity.SecurityPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityPersonRepository extends JpaRepository<SecurityPerson, String> {
    SecurityPerson findBySecPhone(String secPhone);
}