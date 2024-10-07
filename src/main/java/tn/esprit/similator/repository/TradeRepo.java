package tn.esprit.similator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.similator.entity.Trade;

@Repository
public interface TradeRepo extends JpaRepository<Trade, Long> {

}
