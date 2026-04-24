package tn.esprit.spring.diacarebackend.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.Delivery;
import tn.esprit.spring.diacarebackend.entities.DeliveryStatus;
import tn.esprit.spring.diacarebackend.entities.Order;
import tn.esprit.spring.diacarebackend.repository.DeliveryRepository;
import tn.esprit.spring.diacarebackend.services.DeliveryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;

    @Override
    public Delivery createDelivery(Order order) {
        return null;
    }

    @Override
    @Transactional
    public Delivery createDeliveryForOrderId(Long orderId) {
        // Vérifier si elle existe déjà
        Optional<Delivery> existing = deliveryRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setTrackingCode("DEL-" + orderId + "-" + System.currentTimeMillis());
        delivery.setStatus(DeliveryStatus.CONFIRMED.name());
        delivery.setUpdatedAt(LocalDateTime.now());
        delivery.setEstimatedArrival(LocalDateTime.now().plusHours(2));
        delivery.setCarrier("StandardLogistics");
        delivery.setVehicle("Camionnette");

        log.info("Delivery created for orderId: {}", orderId);
        return deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public Delivery updateStatus(Long orderId, DeliveryStatus newStatus) {
        // Créer si n'existe pas
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseGet(() -> createDeliveryForOrderId(orderId));

        delivery.setStatus(newStatus.name());
        delivery.setUpdatedAt(LocalDateTime.now());

        Delivery saved = deliveryRepository.save(delivery);
        log.info("Delivery status updated to {} for orderId: {}", newStatus, orderId);
        return saved;
    }
    @Override
    public Delivery getDeliveryForOrder(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order " + orderId));
    }
    @Override
    public List<Delivery> getAllDeliveries() {
        log.info("Fetching all deliveries");
        return deliveryRepository.findAll();
    }
}