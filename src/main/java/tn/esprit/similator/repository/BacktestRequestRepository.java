package tn.esprit.similator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.similator.entity.BacktestRequest;
import tn.esprit.similator.entity.User;


import java.util.List;

@Repository
public interface BacktestRequestRepository extends JpaRepository<BacktestRequest, Long> {
  List<BacktestRequest> findByUser(User user);
}
