package tn.esprit.spring.diacarebackend.services;

import tn.esprit.spring.diacarebackend.entities.Order;

import java.util.List;

public interface OrderService {

    Order confirmPayment(Long orderId);
    Order createOrder(String token);
    List<Order> getAllOrders();
}