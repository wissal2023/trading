package tn.esprit.similator.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tn.esprit.similator.entity.Policy;
import tn.esprit.similator.repository.PolicyRepository;
import tn.esprit.similator.repository.UserInsuranceRepository;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final UserInsuranceRepository userInsuranceRepository;

    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    public Policy getPolicyById(Long id) {
        return policyRepository.findById(id).orElseThrow(() -> new RuntimeException("Policy not found"));
    }

    public Policy addPolicy(Policy policy) {
        return policyRepository.save(policy);
    }

    public Policy updatePolicy(Long id, Policy updatedPolicy) {
        Policy existingPolicy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        
                // Check if any UserInsurance references this policy
        boolean isPolicyInUse = userInsuranceRepository.existsByPolicyId(id);

        if (isPolicyInUse) {
            throw new RuntimeException("Cannot update policy because it is in use by users");
        }
        // Update fields
        existingPolicy.setName(updatedPolicy.getName());
        existingPolicy.setDescription(updatedPolicy.getDescription());
        existingPolicy.setPremium(updatedPolicy.getPremium());
        existingPolicy.setCoverageDetails(updatedPolicy.getCoverageDetails());
        existingPolicy.setConditions(updatedPolicy.getConditions());
        existingPolicy.setDurationInDays(updatedPolicy.getDurationInDays());

        return policyRepository.save(existingPolicy);
    }

    public void deletePolicy(Long id) {

        policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        
        // Check if any UserInsurance references this policy
        boolean isPolicyInUse = userInsuranceRepository.existsByPolicyId(id);

        if (isPolicyInUse) {
            throw new RuntimeException("Cannot delete policy because it is in use by users");
        }
        policyRepository.deleteById(id);
    }

    public Policy getMostPurchasedPolicy() {
        return policyRepository.findMostPurchasedPolicy(PageRequest.of(0, 1)).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No policies purchased yet"));
    }

    public Policy getBestPolicyInTermsOfValue() {
        return policyRepository.findBestPolicyInTermsOfValue(PageRequest.of(0, 1)).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No policies available"));
    }
    
}
