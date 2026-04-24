package tn.esprit.spring.diacarebackend.services;

import tn.esprit.spring.diacarebackend.entities.Delivery;
import tn.esprit.spring.diacarebackend.entities.DeliveryStatus;
import tn.esprit.spring.diacarebackend.entities.Order;

import java.util.List;
public interface DeliveryService {
        Delivery createDelivery(Order order);
        Delivery createDeliveryForOrderId(Long orderId);  // ← AJOUTE
        Delivery updateStatus(Long orderId, DeliveryStatus newStatus);
        Delivery getDeliveryForOrder(Long orderId);
        List<Delivery> getAllDeliveries();
    }
