package tn.esprit.similator.service;

import tn.esprit.similator.entity.PlacingOrder;

import java.util.List;

public interface IPortfolioRiskAnalysisService {
    double calculatePortfolioVaR(List<PlacingOrder> placingOrders, double confidenceLevel);
}
