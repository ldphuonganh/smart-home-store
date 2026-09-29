package vn.edu.crs.orderservice.entity;

/**
 * Trạng thái thanh toán của đơn (do payment-service báo về qua API nội bộ).
 * COD: UNPAID cho tới khi admin chuyển đơn sang COMPLETED (đã giao, đã thu tiền).
 */
public enum PaymentStatus {
    UNPAID,
    PAID
}
