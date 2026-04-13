package tn.esprit.spring.diacarebackend.serviceImpl;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.services.PaiementService;


@Service
public class PaiementServiceImpl implements PaiementService {
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;
    @Override
    public PaymentIntent createPaymentIntent(Long amount) throws Exception {

        Stripe.apiKey = "stripeSecretKey;"; // 🔥 TON SECRET KEY

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amount)
                        .setCurrency("usd")
                        .build();

        return PaymentIntent.create(params);
    }
}