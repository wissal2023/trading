package tn.esprit.similator.entity;

public class PortfolioRiskManagement {

    private static final double MAX_PORTFOLIO_RISK = 0.05;  // Maximum risk tolerance (5% of portfolio value)
    private static final double MAX_POSITION_SIZE = 0.1;  // Maximum position size (10% of portfolio value)
    private static final double MAX_DRAWDOWN = 0.2;  // Maximum allowable drawdown (20%)

    private Portfolio portfolio;

    public PortfolioRiskManagement(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    // Method to calculate the total portfolio value
    public double calculatePortfolioValue() {
        return portfolio.getAccVal() + portfolio.getCash() + portfolio.getBuyPow();
    }

    // Method to calculate the risk of the portfolio
    public double calculatePortfolioRisk() {
        double totalValue = calculatePortfolioValue();
        double totalRisk = 0.0;

        // Risk assessment logic: calculating exposure to each asset type
        for (Holding holding : portfolio.getHoldings()) {
            double holdingValue = holding.getQty() * holding.getCurntPrice();
            double holdingRisk = holdingValue / totalValue;
            totalRisk += holdingRisk; // This can be modified to factor in volatility, beta, etc.
        }

        return totalRisk;
    }

    // Method to check portfolio risk tolerance
    public boolean isRiskWithinLimits() {
        double portfolioRisk = calculatePortfolioRisk();
        return portfolioRisk <= MAX_PORTFOLIO_RISK;
    }

    // Method to check if any position size is too large
    public boolean isPositionSizeWithinLimits() {
        double totalValue = calculatePortfolioValue();
        for (Holding holding : portfolio.getHoldings()) {
            double positionSize = holding.getQty() * holding.getCurntPrice() / totalValue;
            if (positionSize > MAX_POSITION_SIZE) {
                return false;
            }
        }
        return true;
    }

    // Method to check drawdown risk (e.g., 20% drawdown)
//    public boolean isPortfolioUnderDrawdownLimit() {
//        double portfolioValue = calculatePortfolioValue();
//        double maxPortfolioValue = portfolio.getMaxPortfolioValue(); // Store the peak value of the portfolio
//        double drawdown = (maxPortfolioValue - portfolioValue) / maxPortfolioValue;
//        return drawdown <= MAX_DRAWDOWN;
//    }

    // Method to apply risk controls
//    public boolean applyRiskManagement() {
//        return isRiskWithinLimits() && isPositionSizeWithinLimits() && isPortfolioUnderDrawdownLimit();
//    }

}

