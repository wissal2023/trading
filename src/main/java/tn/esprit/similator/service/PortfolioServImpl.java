package tn.esprit.similator.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.similator.entity.Portfolio;
import tn.esprit.similator.repository.PortfolioRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class PortfolioServImpl implements IPortfolioService{

    PortfolioRepo portfolioRepo;
    @Override
    public List<Portfolio> retrieveAllPortfolios() {
        return portfolioRepo.findAll();
    }

    @Override
    public Portfolio retrievePortfolio(Long portfolioId) {
        return portfolioRepo.findById(portfolioId).get();
    }

    @Override
    public Portfolio addPortfolio(Portfolio p) {
        return portfolioRepo.save(p);
    }

    @Override
    public void removePortfolio(Long portfolioId) {
        portfolioRepo.deleteById(portfolioId);
    }

    @Override
    public Portfolio modifyPortfolio(Portfolio portfolio) {
        return portfolioRepo.save(portfolio);
    }
}
