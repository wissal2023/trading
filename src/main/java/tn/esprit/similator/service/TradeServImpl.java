package tn.esprit.similator.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.similator.entity.Trade;
import tn.esprit.similator.repository.TradeRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class TradeServImpl implements ITradeService {

    TradeRepo tradeRepo;
    @Override
    public List<Trade> retrieveAllTrades() {
        return tradeRepo.findAll();
    }

    @Override
    public Trade retrieveTrade(Long tradeId) {
        return tradeRepo.findById(tradeId).get();
    }

    @Override
    public Trade addTrade(Trade usr) {
        return tradeRepo.save(usr);
    }

    @Override
    public void removeTrade(Long tradeId) {
        tradeRepo.deleteById(tradeId);
    }

    @Override
    public Trade modifyTrade(Trade trade) {
        return tradeRepo.save(trade);
    }
}
