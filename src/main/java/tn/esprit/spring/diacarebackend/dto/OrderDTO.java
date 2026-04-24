package tn.esprit.spring.diacarebackend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private LocalDateTime createdAt;
    private String status;
    private String paymentStatus;
    private Double totalPrice;
    private String patientFirstName;
    private String patientLastName;
    private String patientEmail;
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private Long id;
        private int quantity;
        private double price;
        private ProductDTO product;
    }

    @Data
    public static class ProductDTO {
        private Long id;
        private String name;
        private double price;
        private String image;
    }
}