package tn.esprit.spring.diacarebackend.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.Delivery;
import tn.esprit.spring.diacarebackend.entities.DeliveryStatus;
import tn.esprit.spring.diacarebackend.entities.Order;
import tn.esprit.spring.diacarebackend.repository.DeliveryRepository;
import tn.esprit.spring.diacarebackend.services.DeliveryService;
import tn.esprit.spring.diacarebackend.services.OrderService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderService orderService;   // needed to check order ownership

    @Override
    public Delivery createDelivery(Order order) {
        log.info("Creating delivery for order id: {}", order.getId());

        Delivery delivery = new Delivery();
        delivery.setOrderId(order.getId());
        delivery.setTrackingCode("DEL-" + order.getId() + "-" + System.currentTimeMillis());
        delivery.setStatus(DeliveryStatus.CONFIRMED.name());
        delivery.setUpdatedAt(LocalDateTime.now());

        // Priority for medical products
        boolean hasMedical = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getType().name().equals("MEDICAL"));

        if (hasMedical) {
            delivery.setEstimatedArrival(LocalDateTime.now().plusMinutes(30));
            delivery.setCarrier("ExpressMed");
            delivery.setVehicle("Véhicule réfrigéré");
            delivery.setNotes("Produit médical – livraison prioritaire");
        } else {
            delivery.setEstimatedArrival(LocalDateTime.now().plusHours(2));
            delivery.setCarrier("StandardLogistics");
            delivery.setVehicle("Camionnette");
        }

        return deliveryRepository.save(delivery);
    }

    @Override
    public Delivery updateStatus(Long orderId, DeliveryStatus newStatus) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order " + orderId));
        delivery.setStatus(newStatus.name());
        delivery.setUpdatedAt(LocalDateTime.now());
        return deliveryRepository.save(delivery);
    }

    @Override
    public Delivery getDeliveryForOrder(Long orderId) {
        // Optional: check if current user owns the order (unless admin)
        // You can implement security check here using SecurityContextHolder
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order " + orderId));
    }
}