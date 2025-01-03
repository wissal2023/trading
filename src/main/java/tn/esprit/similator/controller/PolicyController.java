package tn.esprit.similator.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import tn.esprit.similator.entity.Policy;
import tn.esprit.similator.service.PolicyService;

@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
@Tag(name = "Policy")
@CrossOrigin(origins = "*")
public class PolicyController {
    
    private final PolicyService policyService;

    @GetMapping("/")
    public List<Policy> getAllPolicies() {
        return policyService.getAllPolicies();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPolicy(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(policyService.getPolicyById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/")
    public Policy addPolicy(@RequestBody Policy policy) {
        return policyService.addPolicy(policy);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePolicy(@PathVariable Long id, @RequestBody Policy updatedPolicy) {
        try {
            return ResponseEntity.ok(policyService.updatePolicy(id, updatedPolicy));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePolicy(@PathVariable Long id) {
        try {
            policyService.deletePolicy(id);
            return ResponseEntity.ok("Policy deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/most-purchased")
    public ResponseEntity<Policy> getMostPurchasedPolicy() {
        try {

            return ResponseEntity.ok(policyService.getMostPurchasedPolicy());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }


    @GetMapping("/best-value")
    public ResponseEntity<Policy> getBestPolicyInTermsOfValue() {
        return ResponseEntity.ok(policyService.getBestPolicyInTermsOfValue());
    }
}
