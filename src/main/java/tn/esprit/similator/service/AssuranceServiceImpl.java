package tn.esprit.similator.service;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import tn.esprit.similator.entity.PlacingOrder;
import tn.esprit.similator.entity.Policy;
import tn.esprit.similator.repository.AssuranceRepository;
import tn.esprit.similator.repository.PlacingOrderRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class AssuranceServiceImpl implements IAssuranceService {

    private AssuranceRepository assuranceRepository;
    private final IPortfolioRiskAnalysisService portfolioRiskAnalysisService;
    private final PlacingOrderRepo placingOrderRepo;
    private static final double TAUX_DE_PRIME = 0.02; // Taux de prime de 2%

    @Override
    public Policy addContract(Policy contract) {
        return assuranceRepository.save(contract);
    }

    @Override
    public void deleteContract(Long idContract) {
        assuranceRepository.deleteById(idContract);

    }

    @Override
    public Policy updateContract(Policy contract) {
        return assuranceRepository.save(contract);
    }

    @Override
    public List<Policy> getAllContracts() {
        return assuranceRepository.findAll();
    }

    @Override
    public Policy getContract(Long idContract) {
        return assuranceRepository.findById(idContract)
                .orElseThrow(() -> new RuntimeException("Le contrat avec ID " + idContract + " n'existe pas."));
    }
    // Calculer la prime pour un contrat basé sur le portefeuille associé
    @Override
    public double calculateContractPrime(int idPortefeuille, double confidenceLevel) {
        List<PlacingOrder> orders = placingOrderRepo.findByPortfolio_Id(idPortefeuille);

        if (orders.isEmpty()) {
            throw new RuntimeException("No orders found for the given portfolio ID");
        }

        // Calculer la VaR du portefeuille
        double portfolioVaR = portfolioRiskAnalysisService.calculatePortfolioVaR(orders, confidenceLevel);

        // Calculer la prime en utilisant le taux de prime et la VaR
        double prime = portfolioVaR * TAUX_DE_PRIME;

        return prime;
    }
}

