package tn.esprit.pif.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pif.entity.Transaction;
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}