package vn.edu.smarthome.productservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smarthome.productservice.client.OrderClient;
import vn.edu.smarthome.productservice.dto.*;
import vn.edu.smarthome.productservice.entity.Product;
import vn.edu.smarthome.productservice.entity.Review;
import vn.edu.smarthome.productservice.exception.BadRequestException;
import vn.edu.smarthome.productservice.exception.ConflictException;
import vn.edu.smarthome.productservice.exception.ResourceNotFoundException;
import vn.edu.smarthome.productservice.repository.ReviewRepository;
import vn.edu.smarthome.productservice.security.AuthUser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static vn.edu.smarthome.productservice.repository.ProductSpecifications.*;

/**
 * Đánh giá sản phẩm - giống SmartHome Store:
 * chỉ khách đã mua & nhận hàng thành công mới được đánh giá, mỗi người 1 lần / sản phẩm,
 * admin ẩn/hiện hoặc xoá đánh giá.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductService productService;
    private final OrderClient orderClient;

    @Transactional(readOnly = true)
    public List<ReviewResponse> approvedForProduct(Long productId) {
        return reviewRepository.findByProductIdAndApprovedTrueOrderByCreatedAtDesc(productId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReviewEligibility eligibility(Long productId, AuthUser user) {
        productService.findEntity(productId);
        if (user == null || user.userId() == null || user.isAdmin()) {
            return new ReviewEligibility(false, false, false);
        }
        boolean reviewed = reviewRepository.existsByUserIdAndProductId(user.userId(), productId);
        boolean purchased = orderClient.hasPurchased(user.userId(), productId);
        return new ReviewEligibility(purchased && !reviewed, purchased, reviewed);
    }

    /** Id các sản phẩm khách đã đánh giá (trang đơn hàng hiện nhãn "Đã đánh giá"). */
    @Transactional(readOnly = true)
    public List<Long> reviewedProductIds(AuthUser user) {
        return user == null || user.userId() == null ? List.of() : reviewRepository.findProductIdsByUserId(user.userId());
    }

    @Transactional
    public ReviewResponse create(Long productId, AuthUser user, ReviewRequest request) {
        Product product = productService.findEntity(productId);
        if (user == null || user.userId() == null) {
            throw new BadRequestException("Không xác định được người dùng");
        }
        if (reviewRepository.existsByUserIdAndProductId(user.userId(), productId)) {
            throw new ConflictException("Bạn đã đánh giá sản phẩm này rồi.");
        }
        if (!orderClient.hasPurchased(user.userId(), productId)) {
            throw new BadRequestException("Bạn chỉ có thể đánh giá sản phẩm đã mua và nhận hàng thành công.");
        }
        Review review = new Review();
        review.setProduct(product);
        review.setUserId(user.userId());
        review.setUserName(user.name() != null ? user.name() : user.email());
        review.setUserEmail(user.email());
        review.setRating(request.getRating());
        review.setComment(request.getComment() == null || request.getComment().isBlank()
                ? null : request.getComment().trim());
        return toResponse(reviewRepository.save(review));
    }

    // ===== ADMIN =====

    @Transactional(readOnly = true)
    public AdminReviewPage adminList(String search, Integer rating, Boolean approved, int page, int size) {
        Specification<Review> spec = Specification.where(reviewSearch(search))
                .and(reviewRating(rating))
                .and(reviewApproved(approved));
        var result = reviewRepository.findAll(spec, PageRequest.of(Math.max(page, 0),
                Math.max(1, Math.min(size, 100)), Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", reviewRepository.count());
        for (int i = 5; i >= 1; i--) {
            stats.put("rating" + i, reviewRepository.countByRating(i));
        }
        return new AdminReviewPage(PageResponse.from(result.map(this::toResponse)), stats);
    }

    @Transactional
    public ReviewResponse toggle(Long id) {
        Review review = find(id);
        review.setApproved(!review.isApproved());
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void delete(Long id) {
        reviewRepository.delete(find(id));
    }

    private Review find(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá id = " + id));
    }

    private ReviewResponse toResponse(Review r) {
        Product p = r.getProduct();
        return ReviewResponse.builder()
                .id(r.getId())
                .productId(p.getId())
                .productName(p.getName())
                .productSlug(p.getSlug())
                .productImage(p.getImageUrl())
                .userId(r.getUserId())
                .userName(r.getUserName())
                .userEmail(r.getUserEmail())
                .rating(r.getRating())
                .comment(r.getComment())
                .approved(r.isApproved())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
