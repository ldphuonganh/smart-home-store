# DATABASE DESIGN

## 1. Nguyên tắc

Hệ thống áp dụng nguyên tắc **database per service**:

```text
auth-service     -> auth_db
product-service  -> product_db
cart-service     -> cart_db
order-service    -> order_db
payment-service  -> payment_db
```

Mỗi service tự quản lý dữ liệu thuộc domain của mình. Service khác không truy cập trực tiếp database đó mà giao tiếp thông qua API.

## 2. Danh sách database và entity chính

| Service | Database | Entity chính |
|---|---|---|
| Auth | `auth_db` | User, Address, ApiKey, PasswordResetToken |
| Product | `product_db` | Product, Category, ProductImage, Review |
| Cart | `cart_db` | Cart, CartItem, Wishlist, WishlistItem |
| Order | `order_db` | Order, OrderItem, Promotion |
| Payment | `payment_db` | PaymentTransaction |

## 3. Auth DB

### `users`

Lưu thông tin tài khoản:

- `id`
- `full_name`
- `email`
- `password`
- `phone`
- `address`
- `role`
- `enabled`
- `created_at`
- `updated_at`

Role chính:

```text
CUSTOMER
ADMIN
```

### `addresses`

Quản lý các địa chỉ giao hàng của người dùng.

### `api_keys`

Lưu thông tin API Key phục vụ cơ chế Partner/Public API.

### `password_reset_tokens`

Lưu thông tin phục vụ quy trình đặt lại mật khẩu.

## 4. Product DB

### `products`

Thông tin sản phẩm gồm:

- `id`
- `name`
- `slug`
- `brand`
- `material`
- `weight`
- `power`
- `origin`
- `price`
- `description`
- `image_url`
- `stock`
- `active`
- `sold_count`
- `ordered`
- `category_id`
- thời gian tạo/cập nhật

### `categories`

Lưu danh mục sản phẩm.

### `product_images`

Lưu nhiều hình ảnh cho một sản phẩm và thông tin hình ảnh chính.

### `reviews`

Lưu đánh giá của khách hàng đối với sản phẩm.

## 5. Cart DB

### `carts`

Một giỏ hàng gắn với người dùng:

```text
Cart
  |
  | 1 - N
  v
CartItem
```

### `cart_items`

Lưu:

- `product_id`
- `quantity`
- `selected`

Cart Service chỉ lưu thông tin cần cho giỏ hàng. Khi cần thông tin sản phẩm, service gọi Product Service.

### `wishlists`

Wishlist của người dùng.

### `wishlist_items`

Các sản phẩm nằm trong Wishlist.

## 6. Order DB

### `orders`

Đây là bảng trung tâm của Order Service.

Các nhóm dữ liệu chính:

| Nhóm | Dữ liệu |
|---|---|
| Định danh | `id`, `order_number` |
| Khách hàng | `customer_id`, `customer_name`, `customer_email` |
| Tiền | `subtotal`, `discount_amount`, `shipping_fee`, `total_amount` |
| Khuyến mãi | `promotion_id`, `promotion_code` |
| Order | `status` |
| Thanh toán | `payment_method`, `payment_channel`, `payment_status`, `transaction_id`, `paid_at` |
| Giao hàng | tên, SĐT, địa chỉ, khu vực, phương thức và thông tin đơn vị vận chuyển |
| Hủy | `cancelled_at`, `cancel_reason` |
| Thời gian | `created_at`, `updated_at` |

### `order_items`

Một Order có nhiều OrderItem:

```text
orders
   |
   | 1 - N
   v
order_items
```

Các dữ liệu chính:

- `product_id`
- `product_name`
- `product_slug`
- `product_image`
- `price`
- `quantity`
- `subtotal`

`product_name`, `price` và thông tin hiển thị được lưu dạng **snapshot** tại thời điểm đặt hàng.

Ví dụ:

```text
Ngày đặt:
Product A = 1.000.000

Sau đó sản phẩm tăng giá:
Product A = 1.200.000

Order cũ:
OrderItem.price = 1.000.000
```

Điều này bảo đảm lịch sử đơn hàng không bị thay đổi theo giá sản phẩm hiện tại.

### `promotions`

Quản lý mã giảm giá:

- `code`
- `name`
- `type`
- `value`
- `max_discount_amount`
- `min_order_amount`
- `start_date`
- `end_date`
- `usage_limit`
- `used_count`
- `active`

## 7. Payment DB

### `payments`

Payment Service sử dụng entity `PaymentTransaction` để lưu nhật ký giao dịch.

Các dữ liệu chính:

- `id`
- `order_id`
- `order_number`
- `customer_id`
- `method`
- `txn_ref`
- `amount`
- `status`
- `gateway_transaction_id`
- `response_code`
- `message`
- `created_at`
- `updated_at`

Trạng thái giao dịch:

```text
PENDING
SUCCESS
FAILED
CANCELLED
```

Order Service vẫn là nơi quản lý **trạng thái đơn hàng**; Payment Service lưu thông tin giao dịch và kết quả thanh toán để đối soát.

## 8. Quan hệ dữ liệu

Trong cùng service:

```text
Auth DB
User 1 ---- N Address

Product DB
Category 1 ---- N Product
Product 1 ---- N ProductImage
Product 1 ---- N Review

Cart DB
Cart 1 ---- N CartItem
Wishlist 1 ---- N WishlistItem

Order DB
Order 1 ---- N OrderItem
```

Giữa các service **không tạo foreign key trực tiếp qua database**.

Ví dụ:

```text
Order.order.customerId
        |
        X  không FK sang auth_db
        |
        v
Auth Service API
```

và:

```text
OrderItem.productId
        |
        X  không FK sang product_db
        |
        v
Product Service API
```

## 9. Tồn kho và tính nhất quán

Product Service sở hữu dữ liệu `stock`.

Order Service không tự cập nhật `product_db`.

Luồng giữ kho:

```text
Order Service
      |
      v
Product Service
      |
      v
reserve-stock
      |
      v
product_db
```

Nếu Order thất bại sau khi đã giữ kho:

```text
Order Service
      |
      v
release-stock
      |
      v
Product Service
```

Cách này tránh việc một service thao tác trực tiếp database của service khác.

## 10. Ghi chú về transaction phân tán

`@Transactional` trong Order Service không tạo ra một transaction chung cho `order_db` và `product_db`.

Vì vậy:

```text
order_db
    +
product_db
    +
payment_db
```

không thể được rollback bằng một transaction database đơn.

Các nghiệp vụ liên service cần sử dụng trạng thái, xử lý lỗi và compensation phù hợp. Đây là điểm quan trọng của kiến trúc hướng dịch vụ.

## 11. Kiểm tra khi triển khai

Khi triển khai thực tế, schema cuối cùng cần được tạo từ Entity/Migration của từng service. Không nên dùng tài liệu này thay thế migration hoặc schema thực tế của ứng dụng.
