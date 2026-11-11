package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.SecurityPerson;
import com.cjy.bikesystem.service.SecurityPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security-persons")
public class SecurityPersonController {

    @Autowired
    private SecurityPersonService securityPersonService;

    @GetMapping
    public List<SecurityPerson> getAllSecurityPersons() {
        return securityPersonService.getAllSecurityPersons();
    }

    @GetMapping("/{securityId}")
    public ResponseEntity<SecurityPerson> getSecurityPersonById(@PathVariable String securityId) {
        var person = securityPersonService.getSecurityPersonById(securityId);
        return person.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public SecurityPerson addSecurityPerson(@RequestBody SecurityPerson person) {
        return securityPersonService.addSecurityPerson(person);
    }

    @PutMapping("/{securityId}")
    public ResponseEntity<SecurityPerson> updateSecurityPerson(@PathVariable String securityId, @RequestBody SecurityPerson personDetails) {
        SecurityPerson updatedPerson = securityPersonService.updateSecurityPerson(securityId, personDetails);
        return updatedPerson != null ? ResponseEntity.ok(updatedPerson) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{securityId}")
    public ResponseEntity<Map<String, Boolean>> deleteSecurityPerson(@PathVariable String securityId) {
        boolean deleted = securityPersonService.deleteSecurityPerson(securityId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", deleted);
        return deleted ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }
}