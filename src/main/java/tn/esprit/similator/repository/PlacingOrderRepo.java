package tn.esprit.similator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.similator.entity.PlacingOrder;

import java.util.List;

@Repository
public interface PlacingOrderRepo extends JpaRepository<PlacingOrder, Long> {
    List<PlacingOrder> findByPortfolio_Id(long id);
}
