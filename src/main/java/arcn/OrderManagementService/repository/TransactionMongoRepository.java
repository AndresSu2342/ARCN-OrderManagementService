package arcn.OrderManagementService.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import arcn.OrderManagementService.model.Transaction;

@Repository
public interface TransactionMongoRepository extends MongoRepository<Transaction, String> {
    
}
