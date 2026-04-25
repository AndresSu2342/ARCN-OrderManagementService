package arcn.OrderManagementService.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTO Tests")
class DtoTest {

    // ───────────────────────── ProductRequest ─────────────────────────

    @Test
    @DisplayName("ProductRequest @NoArgsConstructor debe crear instancia vacía")
    void testProductRequestNoArgsConstructorCreatesEmptyInstance() {
        // Arrange & Act
        ProductRequest req = new ProductRequest();
        
        // Assert
        assertThat(req).isNotNull();
        assertThat(req.getDishName()).isNull();
        assertThat(req.getQuantity()).isNull();
        assertThat(req.getUnitPrice()).isNull();
        assertThat(req.getCurrency()).isNull();
        assertThat(req.getCustomerReference()).isNull();
    }

    @Test
    @DisplayName("ProductRequest @AllArgsConstructor debe asignar todos los campos")
    void testProductRequestAllArgsConstructorAssignsAllFields() {
        // Arrange & Act
        ProductRequest req = new ProductRequest("Bandeja paisa", 2L, 35000L, "cop", "ref-001");

        // Assert
        assertThat(req.getDishName()).isEqualTo("Bandeja paisa");
        assertThat(req.getQuantity()).isEqualTo(2L);
        assertThat(req.getUnitPrice()).isEqualTo(35000L);
        assertThat(req.getCurrency()).isEqualTo("cop");
        assertThat(req.getCustomerReference()).isEqualTo("ref-001");
    }

    @Test
    @DisplayName("ProductRequest setters y getters deben funcionar correctamente")
    void testProductRequestSettersAndGettersWorkCorrectly() {
        // Arrange
        ProductRequest req = new ProductRequest();
        
        // Act
        req.setDishName("Pollo asado");
        req.setQuantity(1L);
        req.setUnitPrice(18000L);
        req.setCurrency("usd");
        req.setCustomerReference("user-777");

        // Assert
        assertThat(req.getDishName()).isEqualTo("Pollo asado");
        assertThat(req.getQuantity()).isEqualTo(1L);
        assertThat(req.getUnitPrice()).isEqualTo(18000L);
        assertThat(req.getCurrency()).isEqualTo("usd");
        assertThat(req.getCustomerReference()).isEqualTo("user-777");
    }

    // ───────────────────────── StripeResponse ─────────────────────────

    @Test
    @DisplayName("StripeResponse @NoArgsConstructor debe crear instancia vacía")
    void testStripeResponseNoArgsConstructorCreatesEmptyInstance() {
        // Arrange & Act
        StripeResponse resp = new StripeResponse();
        
        // Assert
        assertThat(resp).isNotNull();
        assertThat(resp.getStatus()).isNull();
        assertThat(resp.getMessage()).isNull();
        assertThat(resp.getSessionId()).isNull();
        assertThat(resp.getSessionurl()).isNull();
    }

    @Test
    @DisplayName("StripeResponse @AllArgsConstructor debe asignar todos los campos")
    void testStripeResponseAllArgsConstructorAssignsAllFields() {
        // Arrange & Act
        StripeResponse resp = new StripeResponse("success", "Checkout session created", "cs_test_123", "https://checkout.stripe.com");

        // Assert
        assertThat(resp.getStatus()).isEqualTo("success");
        assertThat(resp.getMessage()).isEqualTo("Checkout session created");
        assertThat(resp.getSessionId()).isEqualTo("cs_test_123");
        assertThat(resp.getSessionurl()).isEqualTo("https://checkout.stripe.com");
    }

    @Test
    @DisplayName("StripeResponse setters y getters deben funcionar correctamente")
    void testStripeResponseSettersAndGettersWorkCorrectly() {
        // Arrange
        StripeResponse resp = new StripeResponse();
        
        // Act
        resp.setStatus("error");
        resp.setMessage("Algo salió mal");
        resp.setSessionId(null);
        resp.setSessionurl(null);

        // Assert
        assertThat(resp.getStatus()).isEqualTo("error");
        assertThat(resp.getMessage()).isEqualTo("Algo salió mal");
        assertThat(resp.getSessionId()).isNull();
        assertThat(resp.getSessionurl()).isNull();
    }

    @Test
    @DisplayName("StripeResponse equals y hashCode (Lombok @Data) deben funcionar")
    void testStripeResponseEqualsAndHashCodeWorkCorrectly() {
        // Arrange
        StripeResponse r1 = new StripeResponse("success", "msg", "id", "url");
        StripeResponse r2 = new StripeResponse("success", "msg", "id", "url");

        // Act & Assert
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }
}
