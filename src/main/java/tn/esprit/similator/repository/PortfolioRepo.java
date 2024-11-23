package tn.esprit.similator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.similator.entity.Portfolio;
import tn.esprit.similator.entity.Status;

import java.util.List;

@Repository
public interface PortfolioRepo extends JpaRepository<Portfolio, Long> {


}
