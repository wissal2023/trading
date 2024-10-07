package tn.esprit.similator.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.similator.entity.Trade;
import tn.esprit.similator.service.ITradeService;

import java.util.List;

@Tag(name = "Trade class")
@RestController
@AllArgsConstructor
@RequestMapping("/trade")
public class TradeController {

    ITradeService tradeServ;
    
    @GetMapping("/Get-all-trades")
    public List<Trade> getTrades() {
        List<Trade> listUtsers = tradeServ.retrieveAllTrades();
        return listUtsers;
    }
    
    @GetMapping("/Get-trade/{trade-id}")
    public Trade retrieveTrade(@PathVariable("trade-id") Long tradeId) {
        Trade trade = tradeServ.retrieveTrade(tradeId);
        return trade;
    }

    @PostMapping("/Add-Trade")
    public Trade addTrade(@RequestBody Trade asst) {
        Trade trade = tradeServ.addTrade(asst);
        return trade;
    }
    @PutMapping("/modify-trade")
    public Trade modifyTrade(@RequestBody Trade asst) {
        Trade trade = tradeServ.modifyTrade(asst);
        return trade;
    }

    @DeleteMapping("/remove-trade/{trade-id}")
    public void removeTrade(@PathVariable("trade-id") Long tradeId) {
        tradeServ.removeTrade(tradeId);
    }


}
