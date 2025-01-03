package tn.esprit.similator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.similator.entity.PlacingOrder;

import java.util.List;

@Service
public class PortfolioRiskAnalysisService implements IPortfolioRiskAnalysisService {
    private final IRiskAnalysisService  riskAnalysisService ;
    private final  IMarketDataSimulator marketDataSimulator ;

    @Autowired
    public PortfolioRiskAnalysisService(RiskAnalysisService riskAnalysisService, MarketDataSimulator marketDataSimulator) {
        this.riskAnalysisService = riskAnalysisService;
        this.marketDataSimulator = marketDataSimulator;
    }

    @Override
    public double calculatePortfolioVaR(List<PlacingOrder> orders, double confidenceLevel) {
        double totalPortfolioValue = calculatePortfolioValue(orders);
        double portfolioVaR = 0;

        // Simuler des rendements pour chaque actif dans le portefeuille
        for (PlacingOrder ordre : orders) {
            List<Double> returns = marketDataSimulator.simulateReturns(30);  // Simuler 30 jours de rendements
            double assetVaR = riskAnalysisService.calculateVaR(returns, confidenceLevel);
            portfolioVaR += assetVaR * (ordre.getQty() * ordre.getPrice() / totalPortfolioValue); // Pondérer par la valeur de l'actif
        }

        return portfolioVaR;
    }

    // Calculer la valeur totale du portefeuille
    private double calculatePortfolioValue(List<PlacingOrder> orders) {
        return orders.stream()
                .mapToDouble(ordre -> ordre.getQty() * ordre.getPrice())
                .sum();
    }
}
