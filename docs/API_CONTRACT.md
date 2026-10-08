# API CONTRACT

Tài liệu này mô tả các API chính của Smart Home Store ở mức Gateway và các API nội bộ quan trọng.

## 1. Quy ước chung

Gateway:

```text
http://localhost:8080
```

Frontend:

```text
http://localhost:5173
```

Các service:

```text
auth-service     http://localhost:8081
product-service  http://localhost:8082
order-service    http://localhost:8083
cart-service     http://localhost:8084
payment-service  http://localhost:8085
```

Request JSON:

```http
Content-Type: application/json
```

API yêu cầu đăng nhập:

```http
Authorization: Bearer <JWT_TOKEN>
```

### HTTP status thường dùng

| Status | Ý nghĩa |
|---:|---|
| 200 | Thành công |
| 201 | Tạo dữ liệu thành công |
| 400 | Dữ liệu đầu vào không hợp lệ |
| 401 | Chưa xác thực/token không hợp lệ |
| 403 | Không có quyền |
| 404 | Không tìm thấy dữ liệu |
| 409 | Xung đột nghiệp vụ, ví dụ hết hàng |
| 500 | Lỗi máy chủ |

## 2. Auth API

Gateway prefix:

```text
/api/auth/**
```

| Method | Endpoint | Chức năng |
|---|---|---|
| POST | `/api/auth/register` | Đăng ký |
| POST | `/api/auth/login` | Đăng nhập |
| POST | `/api/auth/forgot-password` | Quên mật khẩu |
| POST | `/api/auth/reset-password` | Đặt lại mật khẩu |
| GET | `/api/auth/me` | Lấy tài khoản hiện tại |
| PUT | `/api/auth/me` | Cập nhật tài khoản |
| PUT | `/api/auth/me/password` | Đổi mật khẩu |

Các chức năng quản lý địa chỉ:

```text
PUT    /api/auth/addresses/{id}
PUT    /api/auth/addresses/{id}/default
DELETE /api/auth/addresses/{id}
```

Admin:

```text
PUT    /api/admin/users/{id}/status
PUT    /api/admin/users/{id}/toggle-role
DELETE /api/admin/users/{id}
```

API Key:

```text
DELETE /api/api-keys/{id}
```

## 3. Product API

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/products/{id}` | Chi tiết sản phẩm |
| GET | `/api/products/slug/{slug}` | Chi tiết theo slug |
| GET | `/api/products/brands` | Danh sách thương hiệu |
| GET | `/api/products/best-sellers` | Sản phẩm bán chạy |
| GET | `/api/products/latest` | Sản phẩm mới |
| GET | `/api/products/{id}/related` | Sản phẩm liên quan |
| GET | `/api/products/{id}/reviews` | Đánh giá sản phẩm |
| GET | `/api/products/reviews` | Danh sách đánh giá |
| GET | `/api/products/reviews/mine` | Đánh giá của người dùng |
| POST | `/api/products/{id}/reviews` | Gửi đánh giá |
| DELETE | `/api/products/reviews/{reviewId}` | Xóa đánh giá |
| PATCH | `/api/products/reviews/{reviewId}/toggle` | Ẩn/hiện đánh giá |

Các API quản trị sản phẩm bao gồm tạo, sửa, xóa, bật/tắt trạng thái và quản lý hình ảnh.

Category:

```text
GET    /api/categories/{id}
GET    /api/categories/slug/{slug}
PUT    /api/categories/{id}
DELETE /api/categories/{id}
```

Public Product API:

```text
GET /api/public/products/{id}
```

## 4. Cart và Wishlist API

### Cart

```text
POST   /api/cart/items
PUT    /api/cart/items/{id}
DELETE /api/cart/items/{id}
PATCH  /api/cart/items/{id}/select
PUT    /api/cart/select-all
DELETE /api/cart/selected
```

### Wishlist

```text
GET    /api/wishlist/ids
POST   /api/wishlist/toggle/{productId}
POST   /api/wishlist/items
DELETE /api/wishlist/items/{id}
DELETE /api/wishlist/products/{productId}
```

## 5. Order và Checkout API

### Customer

```text
POST /api/orders/quote
POST /api/orders
GET  /api/orders/my
GET  /api/orders/last-payment
GET  /api/orders/{id}
PUT  /api/orders/{id}
PUT  /api/orders/{id}/cancel
POST /api/orders/track
```

### Admin

```text
GET    /api/orders/admin/dashboard
GET    /api/orders/admin/{id}
PUT    /api/orders/admin/{id}/status
PUT    /api/orders/admin/{id}/confirm-payment
PUT    /api/orders/admin/{id}/refunded
DELETE /api/orders/admin/{id}
```

### Promotion

```text
GET    /api/promotions/available
GET    /api/promotions/{id}
PUT    /api/promotions/{id}
PUT    /api/promotions/{id}/toggle
DELETE /api/promotions/{id}
```

### Shipping

```text
GET  /api/shipping/config
GET  /api/shipping/provinces
GET  /api/shipping/districts
GET  /api/shipping/wards
POST /api/shipping/services
```

### Trạng thái Order

```text
PENDING
CONFIRMED
SHIPPING
COMPLETED
CANCELLED
```

Luồng chuyển trạng thái được giới hạn theo nghiệp vụ, không cho phép nhảy tùy ý giữa mọi trạng thái.

### Trạng thái Payment của Order

```text
UNPAID
PENDING_VERIFICATION
PAID
FAILED
REFUND_PENDING
REFUNDED
```

## 6. Payment API

```text
GET  /api/payments/config
GET  /api/payments/qr/{orderId}
POST /api/payments/qr/{orderId}/sandbox-pay
GET  /api/payments/qr/{orderId}/status
POST /api/payments/qr/{orderId}/transferred
POST /api/payments/webhooks/sepay
POST /api/payments/vnpay/{orderId}
GET  /api/payments/vnpay/return
GET  /api/payments/vnpay/ipn
POST /api/payments/paypal/{orderId}
POST /api/payments/paypal/capture
GET  /api/payments/order/{orderId}
```

Payment Service lưu giao dịch trong `payment_db`.

Các phương thức hiện được thể hiện trong code gồm:

```text
QR / chuyển khoản
VNPay
PayPal
```

## 7. API nội bộ giữa các service

### Product Service

Các endpoint nội bộ:

```text
GET   /internal/products/{id}
PATCH /internal/products/{id}/reserve-stock
PATCH /internal/products/{id}/release-stock
PATCH /internal/products/{id}/sold
```

Các endpoint này phục vụ giao tiếp giữa service, không phải API chính cho Frontend.

Ví dụ Checkout:

```text
Order Service
    |
    | PATCH /internal/products/{id}/reserve-stock
    v
Product Service
```

### Order Service

Payment Service có thể gọi các endpoint nội bộ liên quan đến trạng thái thanh toán:

```text
GET /internal/orders/{id}
GET /internal/orders/by-number/{orderNumber}
PUT /internal/orders/{id}/paid
PUT /internal/orders/{id}/payment-status
GET /internal/orders/purchased
GET /internal/orders/users/{userId}/active-count
```

## 8. API Gateway routing

Gateway định tuyến các nhóm chính:

```text
/api/auth/**       -> auth-service
/api/admin/**      -> auth-service
/api/api-keys/**   -> auth-service

/api/products/**   -> product-service
/api/categories/** -> product-service
/api/public/products/** -> product-service

/api/cart/**       -> cart-service
/api/wishlist/**   -> cart-service

/api/orders/**     -> order-service
/api/shipping/**   -> order-service
/api/promotions/** -> order-service

/api/payments/**   -> payment-service
```

Frontend chỉ cần biết Gateway:

```text
React :5173
    |
    v
Gateway :8080
```

thay vì phải biết địa chỉ của từng service.

## 9. Luồng Checkout chính

```text
POST /api/orders
      |
      v
API Gateway
      |
      v
OrderController
      |
      v
OrderService.createOrder()
      |
      v
ProductClient
      |
      v
Product Service
      |
      +--> kiểm tra sản phẩm
      |
      +--> reserve-stock
      |
      v
OrderService
      |
      +--> tạo Order
      +--> tạo OrderItem snapshot
      |
      v
order_db
```

Nếu tạo đơn thất bại sau khi đã giữ kho:

```text
OrderService
    |
    v
releaseStock()
    |
    v
Product Service
    |
    v
release-stock
```

## 10. Bảo mật

- JWT được sử dụng cho các API cần xác thực.
- `customerId/userId` của nghiệp vụ Order lấy từ người dùng đã xác thực, không tin một `userId` tùy ý do client gửi.
- CUSTOMER chỉ được xem/chỉnh sửa đơn thuộc quyền của mình.
- ADMIN có quyền quản lý đơn theo nghiệp vụ.
- API nội bộ giữa service không nên được công khai qua Gateway nếu không cần thiết.
