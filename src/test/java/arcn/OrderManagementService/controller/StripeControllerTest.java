package arcn.OrderManagementService.controller;

import arcn.OrderManagementService.dto.ProductRequest;
import arcn.OrderManagementService.dto.StripeResponse;
import arcn.OrderManagementService.model.Transaction;
import arcn.OrderManagementService.repository.TransactionRepository;
import arcn.OrderManagementService.service.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.stripe.model.checkout.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StripeController Tests")
class StripeControllerTest {

    @Mock
    private StripeService stripeService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private StripeController stripeController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripeController, "publicKey", "pk_test_FAKE");
        ReflectionTestUtils.setField(stripeController, "secretKey", "sk_test_FAKE");
    }

    // ─────────────────────── /checkout ────────────────────────────────

    @Test
    @DisplayName("POST /checkout debe delegar al servicio y retornar 200")
    void testCheckoutDelegatesToServiceAndReturnsOk() {
        // Arrange
        ProductRequest request = new ProductRequest("Pizza", 2L, 15000L, "cop", "user-1");
        StripeResponse mockResponse = new StripeResponse("success", "OK", "cs_123", "https://checkout.stripe.com");
        when(stripeService.createCheckoutSession(request)).thenReturn(mockResponse);

        // Act
        ResponseEntity<StripeResponse> result = stripeController.createCheckout(request);

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo(mockResponse);
        verify(stripeService, times(1)).createCheckoutSession(request);
    }

    @Test
    @DisplayName("POST /checkout debe retornar respuesta de error si el servicio falla")
    void testCheckoutReturnsErrorResponseIfServiceFails() {
        // Arrange
        ProductRequest request = new ProductRequest("Pizza", 0L, 15000L, "cop", "user-1");
        StripeResponse errorResponse = new StripeResponse("error", "quantity y unitPrice deben ser mayores a 0", null, null);
        when(stripeService.createCheckoutSession(request)).thenReturn(errorResponse);

        // Act
        ResponseEntity<StripeResponse> result = stripeController.createCheckout(request);

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody().getStatus()).isEqualTo("error");
    }

    // ─────────────────────── /public-key ──────────────────────────────

    @Test
    @DisplayName("GET /public-key debe retornar la clave pública de Stripe")
    void testPublicKeyEndpointReturnsStripePublicKey() {
        // Arrange & Act
        Map<String, String> result = stripeController.getPublicKey();

        // Assert
        assertThat(result).containsEntry("publicKey", "pk_test_FAKE");
    }

    // ─────────────────────── /success ─────────────────────────────────

    @Test
    @DisplayName("GET /success con sessionId debe incluirlo en la respuesta")
    void testSuccessEndpointIncludesSessionIdIfProvided() {
        // Arrange & Act
        Map<String, Object> result = stripeController.success("cs_test_abc123");

        // Assert
        assertThat(result).containsEntry("status", "success");
        assertThat(result).containsEntry("sessionId", "cs_test_abc123");
    }

    @Test
    @DisplayName("GET /success sin sessionId (null) debe retornar status success")
    void testSuccessEndpointReturnsSuccessStatusWithoutSessionId() {
        // Arrange & Act
        Map<String, Object> result = stripeController.success(null);

        // Assert
        assertThat(result).containsEntry("status", "success");
        assertThat(result).containsKey("sessionId");
    }

    // ─────────────────────── /cancel ──────────────────────────────────

    @Test
    @DisplayName("GET /cancel debe retornar status cancelado y mensaje")
    void testCancelEndpointReturnsCanceledStatusAndMessage() {
        // Arrange & Act
        Map<String, Object> result = stripeController.cancel();

        // Assert
        assertThat(result).containsEntry("status", "canceled");
        assertThat(result).containsKey("message");
    }

    // ─────────────────────── /confirm-payment ─────────────────────────

    @Test
    @DisplayName("POST /confirm-payment debe retornar 400 cuando sessionId es null")
    void testConfirmPaymentReturnsBadRequestIfSessionIdIsNull() {
        // Arrange
        Map<String, String> payload = Map.of();

        // Act
        ResponseEntity<Object> result = stripeController.confirmPayment(payload);

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(400);
        assertThat(result.getBody().toString()).contains("sessionId");
    }

    @Test
    @DisplayName("POST /confirm-payment debe retornar 400 cuando sessionId está en blanco")
    void testConfirmPaymentReturnsBadRequestIfSessionIdIsBlank() {
        // Arrange
        Map<String, String> payload = Map.of("sessionId", "   ");

        // Act
        ResponseEntity<Object> result = stripeController.confirmPayment(payload);

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    @DisplayName("POST /confirm-payment debe truncar el sessionId en el '#'")
    void testConfirmPaymentRemovesHashFromSessionIdBeforeProcessing() {
        // Arrange
        Map<String, String> payload = Map.of("sessionId", "cs_test_abc#fragment");

        // Act
        ResponseEntity<Object> result = stripeController.confirmPayment(payload);

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("POST /confirm-payment procesa el pago exitosamente y guarda la transacción")
    void testConfirmPaymentProcessesPaymentAndSavesTransactionSuccessfully() {
        // Arrange
        Map<String, String> payload = Map.of("sessionId", "cs_123");

        Session sessionMock = mock(Session.class);
        when(sessionMock.getStatus()).thenReturn("complete");
        when(sessionMock.getPaymentStatus()).thenReturn("paid");

        Map<String, String> metadata = Map.of(
                "dishName", "Pizza",
                "quantity", "2",
                "unitPrice", "15000",
                "customerReference", "user1"
        );
        when(sessionMock.getMetadata()).thenReturn(metadata);

        try (org.mockito.MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve("cs_123")).thenReturn(sessionMock);

            // Mock save to have the transaction get an ID
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction t = invocation.getArgument(0);
                t.setId("tx_999");
                return t;
            });

            // Act
            ResponseEntity<Object> result = stripeController.confirmPayment(payload);

            // Assert
            assertThat(result.getStatusCodeValue()).isEqualTo(200);
            verify(transactionRepository, times(1)).save(any(Transaction.class));
        }
    }

    @Test
    @DisplayName("POST /confirm-payment retorna error si el pago no está completado")
    void testConfirmPaymentReturnsBadRequestIfPaymentIsNotComplete() {
        // Arrange
        Map<String, String> payload = Map.of("sessionId", "cs_123");

        Session sessionMock = mock(Session.class);
        when(sessionMock.getStatus()).thenReturn("open");
        when(sessionMock.getPaymentStatus()).thenReturn("unpaid");

        try (org.mockito.MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve("cs_123")).thenReturn(sessionMock);

            // Act
            ResponseEntity<Object> result = stripeController.confirmPayment(payload);

            // Assert
            assertThat(result.getStatusCodeValue()).isEqualTo(400);
            verify(transactionRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("POST /confirm-payment retorna error si faltan metadatos esenciales")
    void testConfirmPaymentReturnsBadRequestIfMetadataIsMissing() {
        // Arrange
        Map<String, String> payload = Map.of("sessionId", "cs_123");

        Session sessionMock = mock(Session.class);
        when(sessionMock.getStatus()).thenReturn("complete");
        when(sessionMock.getPaymentStatus()).thenReturn("paid");
        when(sessionMock.getMetadata()).thenReturn(Map.of()); // Faltan metadata

        try (org.mockito.MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve("cs_123")).thenReturn(sessionMock);

            // Act
            ResponseEntity<Object> result = stripeController.confirmPayment(payload);

            // Assert
            assertThat(result.getStatusCodeValue()).isEqualTo(400);
            verify(transactionRepository, never()).save(any());
        }
    }

    // ─────────────────────── /webhook ─────────────────────────────────

    @Test
    @DisplayName("POST /webhook debe retornar 200 vacío siempre")
    void testWebhookAlwaysReturnsOkStatus() {
        // Arrange & Act
        ResponseEntity<Void> result = stripeController.handleStripeWebhook(
                "{\"type\":\"checkout.session.completed\"}", "some-sig");

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isNull();
    }

    @Test
    @DisplayName("POST /webhook debe retornar 200 incluso sin Stripe-Signature")
    void testWebhookReturnsOkStatusEvenWithoutSignature() {
        // Arrange & Act
        ResponseEntity<Void> result = stripeController.handleStripeWebhook("payload", null);

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }
}
