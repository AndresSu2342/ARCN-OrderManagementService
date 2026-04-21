package arcn.OrderManagementService.controller;

import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;

import arcn.OrderManagementService.dto.ProductRequest;
import arcn.OrderManagementService.dto.StripeResponse;
import arcn.OrderManagementService.model.Transaction;
import arcn.OrderManagementService.repository.TransactionRepository;
import arcn.OrderManagementService.service.StripeService;

@RestController
@RequestMapping("/api/stripe")
@CrossOrigin(origins = "*")
public class StripeController {

    private static final Logger logger = LoggerFactory.getLogger(StripeController.class);
    private static final String STATUS_SUCCESS = "success";
    private static final String MESSAGE = "message";
    private static final String ERROR = "error";

    private final StripeService stripeService;
    private final TransactionRepository transactionRepository;

    @Value("${stripe.publicKey}")
    private String publicKey;

    @Value("${stripe.secretKey}")
    private String secretKey;

    public StripeController(StripeService stripeService, TransactionRepository transactionRepository) {
        this.stripeService = stripeService;
        this.transactionRepository = transactionRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        logger.info("Stripe apiKey inicializada (secretKey configurada: {})", secretKey != null && !secretKey.isBlank());
    }

    @PostMapping("/checkout")
    public ResponseEntity<StripeResponse> createCheckout(@RequestBody ProductRequest request) {
        StripeResponse response = stripeService.createCheckoutSession(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public-key")
    public Map<String, String> getPublicKey() {
        return Map.of("publicKey", publicKey);
    }

    @GetMapping("/success")
    public Map<String, Object> success(@RequestParam(name = "session_id", required = false) String sessionId) {
        return Map.of("status", STATUS_SUCCESS, "sessionId", sessionId);
    }

    @GetMapping("/cancel")
    public Map<String, Object> cancel() {
        return Map.of("status", "canceled", MESSAGE, "Pago cancelado por el usuario");
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<Object> confirmPayment(@RequestBody Map<String, String> payload) {
        try {
            String rawSessionId = payload.get("sessionId");
            logger.info("Confirmando pago. sessionId raw: {}", rawSessionId);

            if (rawSessionId == null || rawSessionId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(ERROR, "sessionId es requerido"));
            }

            int hashIndex = rawSessionId.indexOf('#');
            if (hashIndex != -1) rawSessionId = rawSessionId.substring(0, hashIndex);

            String sessionId = rawSessionId.trim();
            logger.info("SessionId normalizado: {}", sessionId);


            Session session = Session.retrieve(sessionId);

            logger.info("Session recuperada. Status: {}, Payment Status: {}",
                    session.getStatus(), session.getPaymentStatus());

            if (!"complete".equals(session.getStatus()) || !"paid".equals(session.getPaymentStatus())) {
                return ResponseEntity.badRequest().body(Map.of(ERROR, "El pago no ha sido completado"));
            }

                Map<String, String> metadata = session.getMetadata();
                String dishName = metadata.get("dishName");
                String quantityStr = metadata.get("quantity");
                String unitPriceStr = metadata.get("unitPrice");
                String customerReference = metadata.get("customerReference");

                if (dishName == null || quantityStr == null || unitPriceStr == null) {
                return ResponseEntity.badRequest().body(Map.of(ERROR, "Metadata incompleta en la sesión"));
            }

                int quantity = Integer.parseInt(quantityStr);
                double unitPrice = Double.parseDouble(unitPriceStr);
                double amount = quantity * unitPrice;

                Transaction transaction = Transaction.createPurchase(
                    customerReference == null ? "ANON" : customerReference,
                    null,
                    quantity,
                    amount,
                    sessionId,
                    dishName);
                transaction.complete();
                transactionRepository.save(transaction);

            return ResponseEntity.ok(Map.of(
                    STATUS_SUCCESS, true,
                    MESSAGE, "Pago procesado exitosamente",
                    "dishName", dishName,
                    "quantity", quantity,
                    "amount", amount,
                    "transactionId", transaction.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR, "Error al procesar el pago: " + e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleStripeWebhook(@RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        logger.info("Webhook recibido de Stripe");
        logger.debug("Payload: {}", payload);
        return ResponseEntity.ok().build();
    }
}
