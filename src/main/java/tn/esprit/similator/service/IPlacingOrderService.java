package tn.esprit.similator.service;

import tn.esprit.similator.entity.PlacingOrder;

import java.util.List;

public interface IPlacingOrderService {
    public List<PlacingOrder> retrieveAllPlacingOrders();
    public PlacingOrder retrievePlacingOrder(Long placingOrderId);
    public PlacingOrder addPlacingOrder(Long portfolioId, PlacingOrder placingOrder);
    public void removePlacingOrder(Long placingOrderId);
    public PlacingOrder modifyPlacingOrder(PlacingOrder placingOrder);
}
