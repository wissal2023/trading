package tn.esprit.similator.service;

import tn.esprit.similator.entity.Holding;
import tn.esprit.similator.entity.PlacingOrder;
import tn.esprit.similator.entity.Status;

import java.util.List;

public interface IPlacingOrderService {

    public void checkAndExecutePendingOrders();
    public PlacingOrder addPlacingOrderBasedOnMarketStatus(Long portfolioId, PlacingOrder placingOrder);

    public PlacingOrder calculateBuyStock(Long portfolioId, PlacingOrder placingOrder);
    public PlacingOrder calculateSellStock(Long portfolioId, PlacingOrder placingOrder);
    public PlacingOrder calculateCoverStock(Long portfolioId, PlacingOrder placingOrder);
    public PlacingOrder calculateShortStock(Long portfolioId, PlacingOrder placingOrder);
    public PlacingOrder changeStatus(Long orderId, Status newStatus);

    public List<PlacingOrder> getOrdersByPortfolioId(Long portfolioId);
    public List<PlacingOrder> retrieveAllPlacingOrders();
    public PlacingOrder retrievePlacingOrder(Long placingOrderId);
    public void removePlacingOrder(Long placingOrderId);
    public PlacingOrder modifyPlacingOrder(PlacingOrder placingOrder);

   }
