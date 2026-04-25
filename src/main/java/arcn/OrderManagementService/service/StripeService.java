package arcn.OrderManagementService.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import arcn.OrderManagementService.dto.ProductRequest;
import arcn.OrderManagementService.dto.StripeResponse;

@Service
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);
    private static final String CHECKOUT_SESSION_ID_PLACEHOLDER = "{CHECKOUT_SESSION_ID}";
    private static final long MIN_TOTAL_AMOUNT_COP = 2000L;
    private final String successUrl;
    private final String cancelUrl;

    public StripeService(
            @Value("${stripe.secretKey}") String secretKey,
            @Value("${stripe.successUrl}") String successUrl,
            @Value("${stripe.cancelUrl}") String cancelUrl) {
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
        Stripe.apiKey = secretKey;
    }
        public StripeResponse createCheckoutSession(ProductRequest request) {

        try {
            String finalSuccessUrl = buildFinalSuccessUrl(successUrl);

            long quantity = (request.getQuantity() == null) ? 1L : request.getQuantity();
            long unitPrice = (request.getUnitPrice() == null) ? 0L : request.getUnitPrice();
            if (quantity <= 0 || unitPrice <= 0) {
                return new StripeResponse("error", "quantity y unitPrice deben ser mayores a 0", null, null);
            }

            long totalAmount = quantity * unitPrice;
            if ("cop".equalsIgnoreCase(request.getCurrency()) && totalAmount < MIN_TOTAL_AMOUNT_COP) {
                return new StripeResponse(
                        "error",
                        "El total en COP debe ser al menos " + MIN_TOTAL_AMOUNT_COP
                                + " para Stripe Checkout. Sube el precio o la cantidad.",
                        null,
                        null);
            }

            String currency = (request.getCurrency() == null) ? "cop" : request.getCurrency().toLowerCase();
            String dishName = (request.getDishName() == null || request.getDishName().isBlank())
                ? "Plato"
                : request.getDishName();
            String customerReference = (request.getCustomerReference() == null)
                ? "ANON"
                : request.getCustomerReference();

            Map<String, String> metadata = new HashMap<>();
            metadata.put("dishName", dishName);
            metadata.put("quantity", String.valueOf(quantity));
            metadata.put("unitPrice", String.valueOf(unitPrice));
            metadata.put("customerReference", customerReference);
            metadata.put("totalAmount", String.valueOf(totalAmount));

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(finalSuccessUrl)
                    .setCancelUrl(cancelUrl)
                    .putAllMetadata(metadata)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(quantity)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(currency)
                                    .setUnitAmount(unitPrice)
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(dishName)
                                    .setDescription("Compra de platos en restaurante")
                                            .build())
                                    .build())
                            .build())
                    .build();

            Session session = Session.create(params);

            logger.info("✅ Checkout Session creada: ID={}", session.getId());
            logger.info("➡️  URL de pago Stripe: {}", session.getUrl());
            if (logger.isInfoEnabled()) {
                logger.info("🔁 Redirige a: {}", finalSuccessUrl.replace(CHECKOUT_SESSION_ID_PLACEHOLDER, session.getId()));
            }

            return new StripeResponse("success", "Checkout session created", session.getId(), session.getUrl());
        } catch (StripeException e) {
            logger.error("❌ Error creando checkout session: {}", e.getMessage(), e);
            return new StripeResponse("error", e.getMessage(), null, null);
        }
    }

    private static String buildFinalSuccessUrl(String successUrl) {
        String urlSeparator = successUrl.contains("?") ? "&" : "?";
        if (successUrl.contains(CHECKOUT_SESSION_ID_PLACEHOLDER)) return successUrl;
        return successUrl + urlSeparator + "session_id=" + CHECKOUT_SESSION_ID_PLACEHOLDER;
    }
}
