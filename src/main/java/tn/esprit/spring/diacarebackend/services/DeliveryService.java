package tn.esprit.spring.diacarebackend.services;

import tn.esprit.spring.diacarebackend.entities.Delivery;
import tn.esprit.spring.diacarebackend.entities.DeliveryStatus;
import tn.esprit.spring.diacarebackend.entities.Order;

public interface DeliveryService {
    Delivery createDelivery(Order order);
    Delivery updateStatus(Long orderId, DeliveryStatus newStatus);
    Delivery getDeliveryForOrder(Long orderId);
}