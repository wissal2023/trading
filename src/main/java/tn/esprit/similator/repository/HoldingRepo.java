package tn.esprit.similator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.similator.entity.Holding;
import tn.esprit.similator.entity.Portfolio;

import java.util.List;

@Repository
public interface HoldingRepo extends JpaRepository<Holding, Long> {

    Holding findBySymbolAndPortfolio(String symbol, Portfolio portfolio);

    // List<Holding> findByPortfolioId(Long portfolioId);

}
