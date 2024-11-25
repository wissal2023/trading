package tn.esprit.similator.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.similator.entity.Portfolio;
import tn.esprit.similator.repository.HoldingRepo;
import tn.esprit.similator.repository.PortfolioRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class PortfolioServImpl implements IPortfolioService{

    PortfolioRepo portfolioRepo;
    //-------------------CRUD------------------
    public List<Portfolio> retrieveAllPortfolios() {
        return portfolioRepo.findAll();
    }
    public Portfolio retrievePortfolio(Long portfolioId) {
        return portfolioRepo.findById(portfolioId).get();
    }
    public void removePortfolio(Long portfolioId) {
        portfolioRepo.deleteById(portfolioId);
    }
    public Portfolio modifyPortfolio(Portfolio portfolio) {
        return portfolioRepo.save(portfolio);
    }

    //------------------------------------OTHER FUNCTION ---------------------------

   /*
    public Portfolio calculatePortfolioValue(Long portfolioId) throws JSONException {
        // Fetch the user's portfolio
        Portfolio portfolio = portfolioRepo.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        // Fetch the holdings (assets) associated with the portfolio
        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);

        // Initialize total value
        double totalPortfolioValue = 0.0;

        // Loop through each asset in the holdings
        for (Holding holding : holdings) {
            // Fetch real-time price of the asset using the new service
            double realTimePrice = marketService.getRealTimePrice(holding.getSymbol());

            // Calculate the market value of this holding
            double holdingMarketValue = realTimePrice * holding.getQty();
            totalPortfolioValue += holdingMarketValue;
        }

        // Set total portfolio value in the Portfolio object
        portfolio.setTotVal(totalPortfolioValue);

        // Recalculate other financial metrics (e.g., buy power, account value)
        portfolio.setBuyPow(portfolio.getCash() + (totalPortfolioValue * 0.5)); // Example calculation

        // Save the updated portfolio
        portfolioRepo.save(portfolio);

        return portfolio;
    }

    */
}
