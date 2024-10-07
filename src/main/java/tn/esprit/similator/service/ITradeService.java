package tn.esprit.similator.service;

import tn.esprit.similator.entity.Trade;

import java.util.List;

public interface ITradeService {
    public List<Trade> retrieveAllTrades();
    public Trade retrieveTrade(Long tradeId);
    public Trade addTrade(Trade c);
    public void removeTrade(Long tradeId);
    public Trade modifyTrade(Trade trade);
}
