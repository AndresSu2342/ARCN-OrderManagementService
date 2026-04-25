package arcn.OrderManagementService.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Transaction Model Tests")
class TransactionTest {

    // ───────────────────────── createPurchase ─────────────────────────

    @Test
    @DisplayName("createPurchase() debe inicializar todos los campos correctamente")
    void testPurchaseInitializesAllFieldsCorrectly() {
        // Arrange
        String userId = "user-123";
        String walletId = "wallet-456";
        int quantity = 3;
        double amount = 45000.0;
        String sessionId = "cs_test_abc";
        String itemName = "Arepas de choclo";

        // Act
        Transaction tx = Transaction.createPurchase(userId, walletId, quantity, amount, sessionId, itemName);

        // Assert
        assertThat(tx.getUserId()).isEqualTo(userId);
        assertThat(tx.getWalletId()).isEqualTo(walletId);
        assertThat(tx.getType()).isEqualTo(Transaction.TransactionType.PURCHASE);
        assertThat(tx.getQuantity()).isEqualTo(quantity);
        assertThat(tx.getMoneyAmount()).isEqualTo(amount);
        assertThat(tx.getStripeSessionId()).isEqualTo(sessionId);
        assertThat(tx.getDescription()).isEqualTo("Compra de 3 x Arepas de choclo");
        assertThat(tx.getStatus()).isEqualTo(Transaction.TransactionStatus.PENDING);
        assertThat(tx.getCreatedAt()).isNotNull();
        assertThat(tx.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("createPurchase() debe registrar la fecha de creación como ahora")
    void testPurchaseSetsCreatedAtNearCurrentTime() {
        // Arrange
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // Act
        Transaction tx = Transaction.createPurchase("u1", "w1", 1, 5000.0, "sess", "Plato");

        // Assert
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertThat(tx.getCreatedAt()).isAfterOrEqualTo(before).isBeforeOrEqualTo(after);
    }

    // ───────────────────────── complete() ─────────────────────────────

    @Test
    @DisplayName("complete() debe cambiar el estado a COMPLETED y fijar completedAt")
    void testTransactionIsCompletedWithCorrectStatusAndDate() {
        // Arrange
        Transaction tx = Transaction.createPurchase("u", "w", 1, 1000.0, "s", "item");

        // Act
        tx.complete();

        // Assert
        assertThat(tx.getStatus()).isEqualTo(Transaction.TransactionStatus.COMPLETED);
        assertThat(tx.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("complete() debe registrar completedAt como ahora")
    void testTransactionCompletedAtIsNearCurrentTime() {
        // Arrange
        Transaction tx = Transaction.createPurchase("u", "w", 1, 1000.0, "s", "item");
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // Act
        tx.complete();

        // Assert
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertThat(tx.getCompletedAt()).isAfterOrEqualTo(before).isBeforeOrEqualTo(after);
    }

    // ───────────────────────── fail() ─────────────────────────────────

    @Test
    @DisplayName("fail() debe cambiar el estado a FAILED y fijar completedAt")
    void testTransactionIsFailedWithCorrectStatusAndDate() {
        // Arrange
        Transaction tx = Transaction.createPurchase("u", "w", 1, 1000.0, "s", "item");

        // Act
        tx.fail();

        // Assert
        assertThat(tx.getStatus()).isEqualTo(Transaction.TransactionStatus.FAILED);
        assertThat(tx.getCompletedAt()).isNotNull();
    }

    // ───────────────────────── Lombok / setters ────────────────────────

    @Test
    @DisplayName("setters y getters deben funcionar correctamente (Lombok @Data)")
    void testLombokSettersAndGettersWorkCorrectly() {
        // Arrange
        Transaction tx = new Transaction();
        LocalDateTime now = LocalDateTime.now();

        // Act
        tx.setId("id-001");
        tx.setUserId("user-007");
        tx.setWalletId("wlt-007");
        tx.setType(Transaction.TransactionType.REFUND);
        tx.setQuantity(2);
        tx.setMoneyAmount(20000.0);
        tx.setStripeSessionId("cs_refund");
        tx.setDescription("Reembolso");
        tx.setStatus(Transaction.TransactionStatus.CANCELLED);
        tx.setMetadata("{\"extra\":\"data\"}");
        tx.setBookingId("bk-99");
        tx.setCreatedAt(now);
        tx.setCompletedAt(now);

        // Assert
        assertThat(tx.getId()).isEqualTo("id-001");
        assertThat(tx.getUserId()).isEqualTo("user-007");
        assertThat(tx.getWalletId()).isEqualTo("wlt-007");
        assertThat(tx.getType()).isEqualTo(Transaction.TransactionType.REFUND);
        assertThat(tx.getQuantity()).isEqualTo(2);
        assertThat(tx.getMoneyAmount()).isEqualTo(20000.0);
        assertThat(tx.getStripeSessionId()).isEqualTo("cs_refund");
        assertThat(tx.getDescription()).isEqualTo("Reembolso");
        assertThat(tx.getStatus()).isEqualTo(Transaction.TransactionStatus.CANCELLED);
        assertThat(tx.getMetadata()).isEqualTo("{\"extra\":\"data\"}");
        assertThat(tx.getBookingId()).isEqualTo("bk-99");
        assertThat(tx.getCreatedAt()).isEqualTo(now);
        assertThat(tx.getCompletedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("@AllArgsConstructor debe asignar todos los campos en el orden correcto")
    void testAllArgsConstructorAssignsFieldsProperly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // Act
        Transaction tx = new Transaction(
                "id-1", "user-1", "wallet-1",
                Transaction.TransactionType.PURCHASE, 5, 30000.0,
                "cs_1", "desc", Transaction.TransactionStatus.PENDING,
                "{}", "bk-1", now, now);

        // Assert
        assertThat(tx.getId()).isEqualTo("id-1");
        assertThat(tx.getUserId()).isEqualTo("user-1");
        assertThat(tx.getWalletId()).isEqualTo("wallet-1");
        assertThat(tx.getType()).isEqualTo(Transaction.TransactionType.PURCHASE);
        assertThat(tx.getQuantity()).isEqualTo(5);
        assertThat(tx.getMoneyAmount()).isEqualTo(30000.0);
        assertThat(tx.getStripeSessionId()).isEqualTo("cs_1");
        assertThat(tx.getDescription()).isEqualTo("desc");
        assertThat(tx.getStatus()).isEqualTo(Transaction.TransactionStatus.PENDING);
        assertThat(tx.getMetadata()).isEqualTo("{}");
        assertThat(tx.getBookingId()).isEqualTo("bk-1");
        assertThat(tx.getCreatedAt()).isEqualTo(now);
        assertThat(tx.getCompletedAt()).isEqualTo(now);
    }

    // ───────────────────────── Enums ──────────────────────────────────

    @Test
    @DisplayName("TransactionType debe tener los valores correctos")
    void testTransactionTypeContainsExpectedValues() {
        // Arrange & Act & Assert
        assertThat(Transaction.TransactionType.values())
                .containsExactlyInAnyOrder(
                        Transaction.TransactionType.PURCHASE,
                        Transaction.TransactionType.REFUND);
    }

    @Test
    @DisplayName("TransactionStatus debe tener los cuatro estados correctos")
    void testTransactionStatusContainsExpectedValues() {
        // Arrange & Act & Assert
        assertThat(Transaction.TransactionStatus.values())
                .containsExactlyInAnyOrder(
                        Transaction.TransactionStatus.PENDING,
                        Transaction.TransactionStatus.COMPLETED,
                        Transaction.TransactionStatus.FAILED,
                        Transaction.TransactionStatus.CANCELLED);
    }

    @Test
    @DisplayName("@NoArgsConstructor debe crear una instancia vacía")
    void testNoArgsConstructorCreatesEmptyObject() {
        // Arrange & Act
        Transaction tx = new Transaction();
        
        // Assert
        assertThat(tx).isNotNull();
        assertThat(tx.getId()).isNull();
        assertThat(tx.getStatus()).isNull();
    }
}
