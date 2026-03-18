package tn.esprit.spring.diacarebackend.services;

import tn.esprit.spring.diacarebackend.entities.Order;

public interface OrderService {

    Order createOrder(String token);
}