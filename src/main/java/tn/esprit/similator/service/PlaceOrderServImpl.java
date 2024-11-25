package tn.esprit.similator.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.similator.entity.*;
import tn.esprit.similator.repository.HoldingRepo;
import tn.esprit.similator.repository.PlacingOrderRepo;
import tn.esprit.similator.repository.PortfolioRepo;
import tn.esprit.similator.repository.TransactionRepo;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@AllArgsConstructor
@Service
public class PlaceOrderServImpl implements IPlacingOrderService {

    PlacingOrderRepo placingOrderRepo;
    PortfolioRepo portfolioRepo;
    TransactionRepo transactionRepository;
    StockQuoteService stockQuoteService;
    HoldingRepo holdingRepo;

    // scheduler to check the market of the symbol is ope or not = scheduler on the order class
    //@Scheduled(cron = "0 */15 14-21 * * MON-FRI") // Runs every 15 mins between 14:00 and 21:00 (market hours)
    @Scheduled(cron = "0 45 18 * * ?")
    public void checkAndExecutePendingOrders() {
        List<PlacingOrder> pendingOrders = placingOrderRepo.findByStatus(Status.PENDING); // Retrieve pending orders
        for (PlacingOrder order : pendingOrders) {
            // Get market open and close times for the symbol
            Map<String, Object> marketInfo = stockQuoteService.searchStockSymbols(order.getSymbol());
            List<Map<String, String>> bestMatches = (List<Map<String, String>>) marketInfo.get("bestMatches");
            if (bestMatches.isEmpty()) {
                log.warn("No market information found for symbol: " + order.getSymbol());
                continue;
            }
            Map<String, String> match = bestMatches.get(0);
            String marketOpenStr = match.get("5. marketOpen");
            String marketCloseStr = match.get("6. marketClose");
            String timezone = match.get("7. timezone");
            LocalTime marketOpen = LocalTime.parse(marketOpenStr);
            LocalTime marketClose = LocalTime.parse(marketCloseStr);
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timezone));
            LocalTime currentTime = now.toLocalTime();

            // If Market open & placingOrder.status.PENDING
            if (currentTime.isAfter(marketOpen) && currentTime.isBefore(marketClose)) {
                // Retrieve the portfolio for the placingOrder
                Portfolio portfolio = portfolioRepo.findById(order.getPortfolio().getId()).orElseThrow(() -> new RuntimeException("Portfolio not found for order: " + order.getId()));
                Double commissionRate = portfolio.getUser().getCommissionRate();
                // Check if price is null
                if (order.getPrice() == null) {
                    log.warn("Price is null for order: " + order.getId());
                    continue;
                }
                Double totalCost = order.getQty() * order.getPrice(); // Calculate total cost
                // add a transaction and a holding
                addTransaction(order, totalCost, commissionRate);
                updateOrderStatusToFilled(order);
            } else {
                // If market closed
                log.info("Market for " + order.getSymbol() + " is closed. Order remains pending.");
            }
        }
    }

    private void updateOrderStatusToFilled(PlacingOrder placingOrder) {
        placingOrder.setStatus(Status.FILLED);
        placingOrderRepo.save(placingOrder); // Update the order status
    }

    public PlacingOrder addPlacingOrderBasedOnMarketStatus(Long portfolioId, PlacingOrder placingOrder) {
        // Fetch market info from stockQuoteService
        Map<String, Object> marketInfo = stockQuoteService.searchStockSymbols(placingOrder.getSymbol());
        // Check if the API response contains rate limit message
        String apiMessage = (String) marketInfo.get("Information");
        if (apiMessage != null && apiMessage.contains("rate limit")) {
            log.warn("API rate limit reached. Please wait before making further requests.");
            throw new RuntimeException("API rate limit reached. Please wait before making further requests.");
        }
        List<Map<String, String>> bestMatches = (List<Map<String, String>>) marketInfo.get("bestMatches");
        // Check if bestMatches is null or empty
        if (bestMatches == null || bestMatches.isEmpty()) {
            log.warn("No market information found for symbol: " + placingOrder.getSymbol());
            throw new RuntimeException("Market information not available for the symbol.");
        }
        Map<String, String> match = bestMatches.get(0); // Parse market open and close times
        String marketOpenStr = match.get("5. marketOpen");
        String marketCloseStr = match.get("6. marketClose");
        String timezone = match.get("7. timezone");
        // Convert market times to LocalTime
        LocalTime marketOpen = LocalTime.parse(marketOpenStr);
        LocalTime marketClose = LocalTime.parse(marketCloseStr);
        log.info("Market open: " + marketOpen + " close: " + marketClose);// Get current time in the market's timezone
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timezone));
        LocalTime currentTime = now.toLocalTime();
        // Check if the market is open or closed and proceed accordingly
        if (currentTime.isAfter(marketOpen) && currentTime.isBefore(marketClose)) {
            return addOrderWhenMktOpen(portfolioId, placingOrder);
        } else {
            if ("day only".equalsIgnoreCase(placingOrder.getDuration())) {
                log.info("Market is closed. Duration set to 'Good Till Canceled' automatically.");
                placingOrder.setDuration("good till canceled");
                placingOrder.setStatus(Status.PENDING);
                placingOrder.setDate(LocalDateTime.now());
            }
            return addOrderWhenMktClosed(portfolioId, placingOrder);
        }
    }

    public PlacingOrder addOrderWhenMktClosed(Long portfolioId, PlacingOrder placingOrder) {
        Portfolio portfolio = portfolioRepo.findById(portfolioId).orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));
        placingOrder.setPortfolio(portfolio);
        // Fetch current market price for the symbol
        Map<String, Object> stockQuote = stockQuoteService.getStockQuote(placingOrder.getSymbol());
        Map<String, String> globalQuote = (Map<String, String>) stockQuote.get("Global Quote");
        if (globalQuote == null || !globalQuote.containsKey("05. price")) {
            throw new RuntimeException("Market price not available for symbol: " + placingOrder.getSymbol());
        }
        Double marketPrice = Double.valueOf(globalQuote.get("05. price"));
        switch (placingOrder.getOrderType()) {
            case MARKET:
                Float totalCostMarket = (float) (placingOrder.getQty() * marketPrice);
                placingOrder.setPrice(totalCostMarket);
                break;

            case LIMIT:
                if (placingOrder.getPrice() == null) {
                    throw new RuntimeException("Limit price must be set for LIMIT order type.");
                }
                break;
            case STOP_LIMIT:
                if (placingOrder.getStopLoss() == null || placingOrder.getPrice() == null) {
                    throw new RuntimeException("Stop price and limit price must be set for STOP_LIMIT order type.");
                }
                break;
            case TRAILING_STOP:
                if (placingOrder.getStopLoss() == null || placingOrder.getParam() == null) {
                    throw new RuntimeException("Trailing stop parameters must be set for TRAILING_STOP order type.");
                }
                placingOrder.setPrice(marketPrice.floatValue());
                if ("day only".equalsIgnoreCase(placingOrder.getDuration())) {
                    log.warn("Market is closed. 'Day Only' duration orders cannot be processed.");
                    throw new RuntimeException("Market is closed right now. Please retry tomorrow or choose 'Good Till Canceled'.");
                } else if ("good till canceled".equalsIgnoreCase(placingOrder.getDuration())) {
                    if ("$".equals(placingOrder.getParam())) {
                        placingOrder.setTrailingStopPrice(marketPrice - placingOrder.getStopLoss());
                    } else if ("%".equals(placingOrder.getParam())) {
                        placingOrder.setTrailingStopPrice(marketPrice * (1 - placingOrder.getStopLoss() / 100));
                    } else {
                        throw new RuntimeException("Invalid parameter for trailing stop. Use '$' for amount or '%' for percentage.");
                    }
                }
                break;
            default:
                throw new RuntimeException("Unsupported order type: " + placingOrder.getOrderType());
        }

        return placingOrderRepo.save(placingOrder);
    }

    public PlacingOrder addOrderWhenMktOpen(Long portfolioId, PlacingOrder placingOrder) {
        Portfolio portfolio = portfolioRepo.findById(portfolioId).orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));
        placingOrder.setPortfolio(portfolio);
        placingOrder.setStatus(Status.OPEN);
        placingOrder = placingOrderRepo.save(placingOrder);

        // Create transaction for this placingOrder
        Double priceMkt = stockQuoteService.getLatestPrice(placingOrder.getSymbol());
        Double commissionRate = portfolio.getUser().getCommissionRate();
        Transaction transaction = addTransaction(placingOrder, priceMkt, commissionRate);
        transaction.setPlacingOrder(placingOrder);
        transactionRepository.save(transaction);

        // Handle portfolio holdings and update portfolio
        addOrUpdateHolding(portfolio, placingOrder);
        updatePortfolioWithOrder(portfolio, placingOrder, transaction);
        portfolioRepo.save(portfolio);

        return placingOrder;
    }

    private Transaction addTransaction(PlacingOrder placingOrder, Double priceMkt, Double commissionRate) {
        Transaction transaction = new Transaction();
        transaction.setPlacingOrder(placingOrder);
        transaction.setQuantity(placingOrder.getQty());
        transaction.setDate(LocalDateTime.now());
        transaction.setPrice(priceMkt);  // Set the transaction price to the current market price
        transaction.setTotalAmount(placingOrder.getQty() * priceMkt);
        transaction.setCommiss(commissionRate * transaction.getTotalAmount());

        return transactionRepository.save(transaction);
    }

    private void addOrUpdateHolding(Portfolio portfolio, PlacingOrder placingOrder) {
        // Fetch existing holding for this symbol in the portfolio
        Holding holding = holdingRepo.findBySymbolAndPortfolio(placingOrder.getSymbol(), portfolio);

        if (placingOrder.getActionType() == actionType.BUY) {
            if (holding == null) {
                holding = new Holding();
                holding.setSymbol(placingOrder.getSymbol());
                holding.setQty(placingOrder.getQty());
                holding.setAvgPrice(placingOrder.getPrice());
                holding.setAcquisitionDate(new Date());
                holding.setPortfolio(portfolio);
            } else {
                // Update holding quantity and average price
                double newQty = holding.getQty() + placingOrder.getQty();
                double newAvgPrice = ((holding.getQty() * holding.getAvgPrice()) +
                        (placingOrder.getQty() * placingOrder.getPrice())) / newQty;
                holding.setQty(newQty);
                holding.setAvgPrice((float) newAvgPrice);
            }
        } else if (placingOrder.getActionType() == actionType.SELL) {
            if (holding == null || holding.getQty() < placingOrder.getQty()) {
                throw new RuntimeException("Cannot sell. Not enough holdings for this symbol in the portfolio.");
            }
            double newQty = holding.getQty() - placingOrder.getQty();
            holding.setQty(newQty);
            // Remove holding if quantity becomes zero
            if (newQty == 0) {
                holdingRepo.delete(holding);
            }
        }
        // Save the updated or new holding
        if (holding != null) {
            holdingRepo.save(holding);
        }
    }

    private void updatePortfolioWithOrder(Portfolio portfolio, PlacingOrder placingOrder, Transaction transaction) {
        double totalAmount = transaction.getTotalAmount();

        if (placingOrder.getActionType() == actionType.BUY) {
            // Deduct cost of the purchase from cash and buying power
            portfolio.setCash(portfolio.getCash() - totalAmount);
            portfolio.setBuyPow(portfolio.getBuyPow() - totalAmount);
            portfolio.setAccVal(portfolio.getAccVal() + totalAmount); // Update total value to reflect purchase
        } else if (placingOrder.getActionType() == actionType.SELL) {
            // Add sale proceeds to cash and buying power
            portfolio.setCash(portfolio.getCash() + totalAmount);
            portfolio.setBuyPow(portfolio.getBuyPow() + totalAmount);
            portfolio.setAccVal(portfolio.getAccVal() - totalAmount); // Update total value to reflect sale
        }
    }

    public PlacingOrder calculateBuyStock(Long portfolioId, PlacingOrder placingOrder) {
        Portfolio portfolio = portfolioRepo.findById(portfolioId).orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));
        User user = portfolio.getUser();
        Double commissionRate = user.getCommissionRate();
        Map<String, Object> stockQuote = stockQuoteService.getStockQuote(placingOrder.getSymbol());
        Double priceMkt = Double.valueOf(((Map<String, String>) stockQuote.get("Global Quote")).get("05. price"));

        if (placingOrder.getAssetsType() == assetsType.STOCKS && placingOrder.getActionType() == actionType.BUY) {
            if (placingOrder.getOrderType() == orderType.MARKET) {
                Float totalCost = (float) (placingOrder.getQty() * priceMkt);
                placingOrder.setPrice(totalCost);
                placingOrder.setStatus(Status.OPEN);
                updatePortfolioWithOrder(portfolio, placingOrder, addTransaction(placingOrder, priceMkt, commissionRate));
                addOrUpdateHolding(portfolio, placingOrder);
            } else if (placingOrder.getOrderType() == orderType.LIMIT || placingOrder.getOrderType() == orderType.STOP_LIMIT) {
                if (placingOrder.getPrice() <= 0) {
                    throw new RuntimeException("Limit price must be greater than zero.");
                }
                if (priceMkt <= placingOrder.getPrice()) {
                    placingOrder.setStatus(Status.FILLED);
                    Double totalCost = placingOrder.getQty() * placingOrder.getPrice();
                    portfolio.setBuyPow(portfolio.getBuyPow() - totalCost);
                    portfolio.setCash(portfolio.getCash() - totalCost);
                    portfolio.setAccVal(portfolio.getAccVal() + totalCost);

                    addTransaction(placingOrder, Double.valueOf(placingOrder.getPrice()), commissionRate);
                    addOrUpdateHolding(portfolio, placingOrder);
                }
            } else if (placingOrder.getOrderType() == orderType.TRAILING_STOP) {
                if (placingOrder.getStopLoss() <= 0) {
                    throw new RuntimeException("Trailing stop amount must be greater than zero.");
                }
                placingOrder.setPrice((float) (priceMkt - placingOrder.getStopLoss()));
                Double highestPrice = priceMkt;

                // Simulate price tracking loop (ensure an appropriate mechanism in production)
                while (true) {
                    priceMkt = stockQuoteService.getLatestPrice(placingOrder.getSymbol());
                    if (priceMkt > highestPrice) {
                        highestPrice = priceMkt;
                        placingOrder.setPrice((float) (highestPrice - placingOrder.getStopLoss()));
                    }
                    if (priceMkt <= placingOrder.getPrice()) {
                        placingOrder.setStatus(Status.FILLED);
                        updatePortfolioWithOrder(portfolio, placingOrder, addTransaction(placingOrder, Double.valueOf(placingOrder.getPrice()), commissionRate));
                        break;
                    }
                }
            }
        }
        return placingOrder;
    }

    public PlacingOrder calculateSellStock(Long portfolioId, PlacingOrder placingOrder) {
        Portfolio portfolio = portfolioRepo.findById(portfolioId).orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));
        User user = portfolio.getUser();
        Double commissionRate = user.getCommissionRate();
        Map<String, Object> stockQuote = stockQuoteService.getStockQuote(placingOrder.getSymbol());
        Double priceMkt = Double.valueOf(((Map<String, String>) stockQuote.get("Global Quote")).get("05. price"));

        if (placingOrder.getAssetsType() == assetsType.STOCKS && placingOrder.getActionType() == actionType.SELL) {
            if (placingOrder.getOrderType() == orderType.MARKET) {
                Float totalCost = (float) (placingOrder.getQty() * priceMkt);
                placingOrder.setPrice(totalCost);
                placingOrder.setStatus(Status.OPEN);
                updatePortfolioWithOrder(portfolio, placingOrder, addTransaction(placingOrder, priceMkt, commissionRate));
                addOrUpdateHolding(portfolio, placingOrder);
            } else if (placingOrder.getOrderType() == orderType.LIMIT) {
                if (placingOrder.getPrice() <= 0) {
                    throw new RuntimeException("Limit price must be greater than zero.");
                }
                if (priceMkt >= placingOrder.getPrice()) {
                    placingOrder.setStatus(Status.FILLED);
                    Double totalCost = placingOrder.getQty() * placingOrder.getPrice();
                    portfolio.setCash(portfolio.getCash() + totalCost);
                    portfolio.setAccVal(portfolio.getAccVal() - totalCost);

                    addTransaction(placingOrder, Double.valueOf(placingOrder.getPrice()), commissionRate);
                    addOrUpdateHolding(portfolio, placingOrder);
                }
            } else if (placingOrder.getOrderType() == orderType.TRAILING_STOP) {
                if (placingOrder.getStopLoss() <= 0) {
                    throw new RuntimeException("Trailing stop amount must be greater than zero.");
                }
                placingOrder.setPrice((float) (priceMkt + placingOrder.getStopLoss()));
                Double lowestPrice = priceMkt;

                // Simulate price tracking loop for selling
                while (true) {
                    priceMkt = stockQuoteService.getLatestPrice(placingOrder.getSymbol());
                    if (priceMkt < lowestPrice) {
                        lowestPrice = priceMkt;
                        placingOrder.setPrice((float) (lowestPrice + placingOrder.getStopLoss()));
                    }
                    if (priceMkt >= placingOrder.getPrice()) {
                        placingOrder.setStatus(Status.FILLED);
                        updatePortfolioWithOrder(portfolio, placingOrder, addTransaction(placingOrder, Double.valueOf(placingOrder.getPrice()), commissionRate));
                        break;
                    }
                }
            }
        }
        return placingOrder;
    }

    public PlacingOrder calculateCoverStock(Long portfolioId, PlacingOrder placingOrder) {
        Portfolio portfolio = portfolioRepo.findById(portfolioId).orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));
        User user = portfolio.getUser();
        Double commissionRate = user.getCommissionRate();
        Map<String, Object> stockQuote = stockQuoteService.getStockQuote(placingOrder.getSymbol());
        Double priceMkt = Double.valueOf(((Map<String, String>) stockQuote.get("Global Quote")).get("05. price"));

        if (placingOrder.getAssetsType() == assetsType.STOCKS && placingOrder.getActionType() == actionType.COVER) {
            if (placingOrder.getOrderType() == orderType.MARKET) {
                Float totalCost = (float) (placingOrder.getQty() * priceMkt);
                placingOrder.setPrice(totalCost);
                placingOrder.setStatus(Status.OPEN);
                updatePortfolioWithOrder(portfolio, placingOrder, addTransaction(placingOrder, priceMkt, commissionRate));
                addOrUpdateHolding(portfolio, placingOrder);
            } else if (placingOrder.getOrderType() == orderType.LIMIT) {
                if (placingOrder.getPrice() <= 0) {
                    throw new RuntimeException("Limit price must be greater than zero.");
                }
                if (priceMkt <= placingOrder.getPrice()) {
                    placingOrder.setStatus(Status.FILLED);
                    Double totalCost = placingOrder.getQty() * placingOrder.getPrice();
                    portfolio.setBuyPow(portfolio.getBuyPow() - totalCost);
                    portfolio.setCash(portfolio.getCash() - totalCost);
                    portfolio.setAccVal(portfolio.getAccVal() + totalCost);

                    addTransaction(placingOrder, Double.valueOf(placingOrder.getPrice()), commissionRate);
                    addOrUpdateHolding(portfolio, placingOrder);
                }
            } else if (placingOrder.getOrderType() == orderType.TRAILING_STOP) {
                if (placingOrder.getStopLoss() <= 0) {
                    throw new RuntimeException("Trailing stop amount must be greater than zero.");
                }
                placingOrder.setPrice((float) (priceMkt + placingOrder.getStopLoss()));
                Double lowestPrice = priceMkt;

                // Simulate price tracking loop for covering the short position
                while (true) {
                    priceMkt = stockQuoteService.getLatestPrice(placingOrder.getSymbol());
                    if (priceMkt < lowestPrice) {
                        lowestPrice = priceMkt;
                        placingOrder.setPrice((float) (lowestPrice + placingOrder.getStopLoss()));
                    }
                    if (priceMkt >= placingOrder.getPrice()) {
                        placingOrder.setStatus(Status.FILLED);
                        updatePortfolioWithOrder(portfolio, placingOrder, addTransaction(placingOrder, Double.valueOf(placingOrder.getPrice()), commissionRate));
                        break;
                    }
                }
            }
        }
        return placingOrder;
    }

    public PlacingOrder calculateShortStock(Long portfolioId, PlacingOrder placingOrder) {
        Portfolio portfolio = portfolioRepo.findById(portfolioId).orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));
        User user = portfolio.getUser();
        Double commissionRate = user.getCommissionRate();
        Map<String, Object> stockQuote = stockQuoteService.getStockQuote(placingOrder.getSymbol());
        Double priceMkt = Double.valueOf(((Map<String, String>) stockQuote.get("Global Quote")).get("05. price"));

        if (placingOrder.getAssetsType() == assetsType.STOCKS && placingOrder.getActionType() == actionType.SHORT) {
            if (placingOrder.getOrderType() == orderType.MARKET) {
                Float totalCost = (float) (placingOrder.getQty() * priceMkt);
                placingOrder.setPrice(totalCost);
                placingOrder.setStatus(Status.OPEN);
                updatePortfolioWithOrder(portfolio, placingOrder, addTransaction(placingOrder, priceMkt, commissionRate));
                addOrUpdateHolding(portfolio, placingOrder);
            } else if (placingOrder.getOrderType() == orderType.LIMIT) {
                if (placingOrder.getPrice() <= 0) {
                    throw new RuntimeException("Limit price must be greater than zero.");
                }
                if (priceMkt >= placingOrder.getPrice()) {
                    placingOrder.setStatus(Status.FILLED);
                    Double totalCost = placingOrder.getQty() * placingOrder.getPrice();
                    portfolio.setCash(portfolio.getCash() + totalCost);
                    portfolio.setAccVal(portfolio.getAccVal() - totalCost);

                    addTransaction(placingOrder, Double.valueOf(placingOrder.getPrice()), commissionRate);
                    addOrUpdateHolding(portfolio, placingOrder);
                }
            } else if (placingOrder.getOrderType() == orderType.TRAILING_STOP) {
                if (placingOrder.getStopLoss() <= 0) {
                    throw new RuntimeException("Trailing stop amount must be greater than zero.");
                }
                placingOrder.setPrice((float) (priceMkt - placingOrder.getStopLoss()));
                Double highestPrice = priceMkt;

                // Simulate price tracking loop for short selling
                while (true) {
                    priceMkt = stockQuoteService.getLatestPrice(placingOrder.getSymbol());
                    if (priceMkt > highestPrice) {
                        highestPrice = priceMkt;
                        placingOrder.setPrice((float) (highestPrice - placingOrder.getStopLoss()));
                    }
                    if (priceMkt <= placingOrder.getPrice()) {
                        placingOrder.setStatus(Status.FILLED);
                        updatePortfolioWithOrder(portfolio, placingOrder, addTransaction(placingOrder, Double.valueOf(placingOrder.getPrice()), commissionRate));
                        break;
                    }
                }
            }
        }
        return placingOrder;
    }


    public List<PlacingOrder> getOrdersByPortfolioId(Long portfolioId) {
        return placingOrderRepo.findByPortfolioId(portfolioId);
    }

    public List<PlacingOrder> retrieveAllPlacingOrders() {
        return placingOrderRepo.findAll();
    }

    public PlacingOrder retrievePlacingOrder(Long placingOrderId) {
        return placingOrderRepo.findById(placingOrderId).get();
    }

    public void removePlacingOrder(Long placingOrderId) {
        placingOrderRepo.deleteById(placingOrderId);
    }

    public PlacingOrder modifyPlacingOrder(PlacingOrder placingOrder) {
        return placingOrderRepo.save(placingOrder);
    }

    public PlacingOrder changeStatus(Long orderId, Status newStatus) {
        Optional<PlacingOrder> orderOptional = placingOrderRepo.findById(orderId);
        PlacingOrder order = orderOptional.get();
        Status currentStatus = order.getStatus();
        if (currentStatus == Status.PENDING) {
            if (newStatus == Status.CANCELLED) {
                order.setStatus(newStatus);
            } else {
                throw new IllegalStateException("Invalid status change from PENDING.");
            }
        } else if (currentStatus == Status.OPEN) {
            if (newStatus == Status.FILLED || newStatus == Status.CANCELLED) {
                order.setStatus(newStatus); // Valid transition, update status
            } else {
                throw new IllegalStateException("Invalid status change from OPEN.");
            }
        } else if (currentStatus == Status.FILLED) {
            throw new IllegalStateException("Cannot change status of a FILLED order.");
        } else if (currentStatus == Status.CANCELLED) {
            throw new IllegalStateException("Cannot change status of a CANCELLED order.");
        }
        return placingOrderRepo.save(order);
    }



}