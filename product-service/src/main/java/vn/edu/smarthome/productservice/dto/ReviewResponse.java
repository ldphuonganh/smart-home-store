package vn.edu.smarthome.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String productImage;
    private Long userId;
    private String userName;
    private String userEmail;
    private int rating;
    private String comment;
    private boolean approved;
    private LocalDateTime createdAt;
}
