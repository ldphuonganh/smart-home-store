package vn.edu.smarthome.paymentservice.entity;

/**
 * COD: thanh toán khi nhận hàng (payment ở PENDING, order-service đánh dấu PAID khi giao xong).
 * BANK_TRANSFER: chuyển khoản / quét QR (giả lập xác nhận trong đồ án).
 * MoMo không nằm trong phạm vi (cần URL callback công khai, không chạy được trên localhost).
 */
public enum PaymentMethod {
    COD,
    BANK_TRANSFER
}
