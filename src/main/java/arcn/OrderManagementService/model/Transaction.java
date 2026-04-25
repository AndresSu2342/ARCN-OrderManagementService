package arcn.OrderManagementService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {
    
    @Id
    private String id;
    
    @Indexed
    private String userId; // sub de Cognito
    
    @Indexed
    private String walletId; // Referencia a la wallet
    
    private TransactionType type; // PURCHASE, USAGE, REFUND
    
    private Integer quantity; // Cantidad de platos u objetos comprados
    
    private Double moneyAmount; // Cantidad en pesos (para compras)
    
    private String stripeSessionId; // ID de sesión de Stripe (si aplica)
    
    private String description; // Descripción de la transacción
    
    private TransactionStatus status; // PENDING, COMPLETED, FAILED, CANCELLED
    
    private String metadata; // Información adicional en formato JSON (opcional)

    @Indexed
    private String bookingId; // Campo legado, no usado para compras de restaurante
    
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
    
    // Enums para tipos y estados
    public enum TransactionType {
        PURCHASE,
        REFUND
    }
    
    public enum TransactionStatus {
        PENDING,    // Transacción pendiente
        COMPLETED,  // Transacción completada
        FAILED,     // Transacción fallida
        CANCELLED   // Transacción cancelada
    }
    
    // Constructor para crear una compra
    public static Transaction createPurchase(String userId, String walletId, 
                                            Integer quantity, Double amount,
                                            String stripeSessionId,
                                            String itemName) {
        Transaction transaction = new Transaction();
        transaction.userId = userId;
        transaction.walletId = walletId;
        transaction.type = TransactionType.PURCHASE;
        transaction.quantity = quantity;
        transaction.moneyAmount = amount;
        transaction.stripeSessionId = stripeSessionId;
        transaction.description = "Compra de " + quantity + " x " + itemName;
        transaction.status = TransactionStatus.PENDING;
        transaction.createdAt = LocalDateTime.now();
        return transaction;
    }
    
    // Método para completar una transacción
    public void complete() {
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    // Método para fallar una transacción
    public void fail() {
        this.status = TransactionStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }
}
