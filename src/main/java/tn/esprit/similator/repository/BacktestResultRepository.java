package tn.esprit.similator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.similator.entity.BacktestResult;

@Repository
public interface BacktestResultRepository extends JpaRepository<BacktestResult, Long> {


}

