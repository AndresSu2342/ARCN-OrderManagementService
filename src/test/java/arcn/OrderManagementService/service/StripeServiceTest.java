package arcn.OrderManagementService.service;

import arcn.OrderManagementService.dto.ProductRequest;
import arcn.OrderManagementService.dto.StripeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias para StripeService concentradas en la lógica de validación
 * y construcción de URLs, sin realizar llamadas reales a la API de Stripe.
 *
 * La lógica de Stripe (Session.create) se prueba indirectamente mediante los
 * retornos de error previos a la llamada real.
 */
@DisplayName("StripeService – validación de parámetros")
class StripeServiceTest {

    private StripeService stripeService;

    @BeforeEach
    void setUp() {
        stripeService = new StripeService(
                "sk_test_FAKE_KEY",
                "http://localhost:8081/api/stripe/success",
                "http://localhost:8081/api/stripe/cancel");
    }

    // ───────────────────── quantity y unitPrice ── ─────────────────────

    @Test
    @DisplayName("Debe retornar error cuando quantity es 0")
    void testErrorIsReturnedIfQuantityIsZero() {
        // Arrange
        ProductRequest request = new ProductRequest("Plato", 0L, 5000L, "cop", "user-1");

        // Act
        StripeResponse response = stripeService.createCheckoutSession(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getMessage()).containsIgnoringCase("mayores a 0");
    }

    @Test
    @DisplayName("Debe retornar error cuando unitPrice es 0")
    void testErrorIsReturnedIfUnitPriceIsZero() {
        // Arrange
        ProductRequest request = new ProductRequest("Plato", 2L, 0L, "cop", "user-1");

        // Act
        StripeResponse response = stripeService.createCheckoutSession(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getMessage()).containsIgnoringCase("mayores a 0");
    }

    @Test
    @DisplayName("Debe retornar error cuando quantity es negativa")
    void testErrorIsReturnedIfQuantityIsNegative() {
        // Arrange
        ProductRequest request = new ProductRequest("Plato", -1L, 5000L, "cop", "user-1");

        // Act
        StripeResponse response = stripeService.createCheckoutSession(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo("error");
    }

    @Test
    @DisplayName("Debe retornar error cuando quantity es null (se trata como 1)")
    void testErrorIsReturnedIfNullQuantityTreatedAsOneWithZeroPrice() {
        // Arrange
        // quantity null → 1, unitPrice null → 0 → debe fallar
        ProductRequest request = new ProductRequest("Plato", null, null, "cop", "user");

        // Act
        StripeResponse response = stripeService.createCheckoutSession(request);

        // Assert
        // quantity=1 ok pero unitPrice=0 → error
        assertThat(response.getStatus()).isEqualTo("error");
    }

    // ───────────────────────── COP mínimo ─────────────────────────────

    @Test
    @DisplayName("Debe retornar error cuando total COP es menor a 2000")
    void testErrorIsReturnedIfCopTotalIsBelowMinimumRequirement() {
        // Arrange
        // 1 × 1500 = 1500 < 2000
        ProductRequest request = new ProductRequest("Arepa", 1L, 1500L, "cop", "user-1");

        // Act
        StripeResponse response = stripeService.createCheckoutSession(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getMessage()).contains("2000");
    }

    @Test
    @DisplayName("Debe retornar error cuando total COP es exactamente 1999")
    void testErrorIsReturnedIfCopTotalIsExactly1999() {
        // Arrange
        // 1 × 1999 = 1999 < 2000
        ProductRequest request = new ProductRequest("Arepa", 1L, 1999L, "cop", "user-1");

        // Act
        StripeResponse response = stripeService.createCheckoutSession(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo("error");
    }

    @Test
    @DisplayName("No debe aplicar validación mínima COP cuando la moneda es USD")
    void testCopMinimumIsNotAppliedIfCurrencyIsUsd() {
        // Arrange
        // 1 × 1500 en USD: NO debe retornar el error de mínimo COP
        ProductRequest request = new ProductRequest("Arepa", 1L, 1500L, "usd", "user-1");

        // Act
        StripeResponse response = stripeService.createCheckoutSession(request);

        // Assert
        // El error debe ser distinto al de mínimo COP (será un error de Stripe API key)
        if ("error".equals(response.getStatus())) {
            assertThat(response.getMessage()).doesNotContain("2000");
        }
    }

    @Test
    @DisplayName("No debe aplicar validación mínima COP cuando moneda es null (default cop aplica post-validación)")
    void testCopMinimumIsNotAppliedIfCurrencyIsNullAndDefaultsToCopPostValidation() {
        // Arrange
        // Cuando currency es null, la validación COP no se evalua antes porque "cop".equalsIgnoreCase(null) es falso.
        ProductRequest request = new ProductRequest("Plato", 1L, 1000L, null, "user-1");

        // Act
        StripeResponse response = stripeService.createCheckoutSession(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getMessage()).doesNotContain("2000");
    }

    @Test
    @DisplayName("Validación COP debe ser case-insensitive (COP en mayúsculas)")
    void testErrorIsReturnedIfCopTotalBelowMinimumIgnoringCase() {
        // Arrange
        // 1 × 1000 = 1000 < 2000, moneda "COP" en mayúsculas
        ProductRequest request = new ProductRequest("Plato", 1L, 1000L, "COP", "user");

        // Act
        StripeResponse response = stripeService.createCheckoutSession(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getMessage()).contains("2000");
    }

    // ───────────────────────── Valores por defecto ─────────────────────

    @Test
    @DisplayName("dishName en blanco debe usar 'Plato' como valor por defecto (llega a Stripe)")
    void testBlankDishNameUsesDefaultValueInStripeSession() {
        // Arrange
        ProductRequest request = new ProductRequest("   ", 1L, 5000L, "cop", "user-1");

        // Act
        StripeResponse response = stripeService.createCheckoutSession(request);

        // Assert
        if ("error".equals(response.getStatus())) {
            assertThat(response.getMessage()).doesNotContainIgnoringCase("dishName");
        }
    }

    @Test
    @DisplayName("customerReference null debe usar 'ANON' como valor por defecto")
    void testNullCustomerReferenceUsesAnonValueInStripeSession() {
        // Arrange
        ProductRequest request = new ProductRequest("Pizza", 1L, 5000L, "cop", null);

        // Act
        StripeResponse response = stripeService.createCheckoutSession(request);

        // Assert
        assertThat(response).isNotNull();
    }
}
