package tn.esprit.similator.service;



import tn.esprit.similator.entity.Policy;

import java.util.List;

public interface IAssuranceService {

    public Policy addContract(Policy policy);
    public void deleteContract(Long id);
    public Policy updateContract(Policy policy);
    public List<Policy> getAllContracts();
    public Policy getContract(Long id) ;
    public double calculateContractPrime(int idPortfolio, double confidenceLevel);
    }




