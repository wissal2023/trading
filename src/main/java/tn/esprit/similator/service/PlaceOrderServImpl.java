package tn.esprit.similator.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.similator.entity.PlacingOrder;
import tn.esprit.similator.entity.Portfolio;
import tn.esprit.similator.repository.PlacingOrderRepo;
import tn.esprit.similator.repository.PortfolioRepo;
import tn.esprit.similator.repository.TransactionRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class PlaceOrderServImpl implements IPlacingOrderService{

    PlacingOrderRepo placingOrderRepo;
    PortfolioRepo portfolioRepo;
    TransactionRepo transactionRepository;

    public List<PlacingOrder> retrieveAllPlacingOrders() {
        return placingOrderRepo.findAll();
    }

    public PlacingOrder retrievePlacingOrder(Long placingOrderId) {
        return placingOrderRepo.findById(placingOrderId).get();
    }
    public PlacingOrder addPlacingOrder(Long portfolioId, PlacingOrder placingOrder) {
        Portfolio portfolio = portfolioRepo.findById(portfolioId).orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));
        placingOrder.setPortfolio(portfolio);
        return placingOrderRepo.save(placingOrder);
    }
    public void removePlacingOrder(Long placingOrderId) {
        placingOrderRepo.deleteById(placingOrderId);
    }

    public PlacingOrder modifyPlacingOrder(PlacingOrder placingOrder) {
        return placingOrderRepo.save(placingOrder);
    }

}
