package arcn.OrderManagementService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    private String dishName;
    private Long quantity;
    private Long unitPrice;
    private String currency;
    private String customerReference;
}
