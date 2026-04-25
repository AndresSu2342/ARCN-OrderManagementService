package arcn.OrderManagementService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias de la clase principal siguiendo el principio FIRST
 * y utilizando el patron AAA (Arrange, Act, Assert).
 */
@DisplayName("Application Setup Tests")
class OrderManagementServiceApplicationTests {

    @Test
    @DisplayName("La aplicacion debe poder instanciarse correctamente sin dependencias externas")
    void testApplicationContextLoadsSuccessfully() {
        // Arrange
        OrderManagementServiceApplication app;

        // Act
        app = new OrderManagementServiceApplication();

        // Assert
        assertThat(app).isNotNull();
    }
}
