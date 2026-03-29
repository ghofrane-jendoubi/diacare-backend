package tn.esprit.spring.diacarebackend.services;

import com.stripe.model.PaymentIntent;

public interface PaiementService {

    PaymentIntent createPaymentIntent(Long amount) throws Exception;
}