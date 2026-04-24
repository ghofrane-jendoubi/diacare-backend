package tn.esprit.spring.diacarebackend.services;

import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.dto.OrderDTO;
import tn.esprit.spring.diacarebackend.entities.Order;

import java.util.List;

public interface OrderService {

    Order confirmPayment(Long orderId);
    Order createOrder(Long patientId);
    List<Order> getAllOrders();
    void deleteOrder(Long id);

    Order markAsPaid(Long id, String email);
    Order getOrderById(Long id);
    Order confirmOrder(Long id);
    Order cancelOrder(Long id);
     void markOrderAsPaid(Long orderId);
    List<Order> getPaidOrders();
    List<OrderDTO> getOrdersByPatient(Long patientId);

}