# DATABASE DESIGN

## 1. Cơ sở dữ liệu

| Nội dung | Giá trị |
|---|---|
| Tên cơ sở dữ liệu | `order_db` |
| Hệ quản trị cơ sở dữ liệu | MySQL |
| Service sử dụng | `order-service` |
| Port của service | `8083` |

Order Service quản lý dữ liệu đơn hàng độc lập trong cơ sở dữ liệu `order_db`.

## 2. Các bảng chính

### 2.1. Bảng `orders`

Bảng `orders` lưu thông tin tổng quát của một đơn hàng.

| Cột | Kiểu dữ liệu tham khảo | Ý nghĩa |
|---|---|---|
| id | BIGINT | Khóa chính của đơn hàng |
| customer_id | BIGINT | Mã khách hàng đặt hàng |
| shipping_address | VARCHAR/TEXT | Địa chỉ giao hàng |
| phone | VARCHAR | Số điện thoại nhận hàng |
| payment_method | VARCHAR | Phương thức thanh toán |
| status | VARCHAR/ENUM | Trạng thái đơn hàng |
| total_amount | DECIMAL | Tổng tiền đơn hàng |
| created_at | DATETIME | Thời điểm tạo đơn |
| updated_at | DATETIME | Thời điểm cập nhật |

### 2.2. Bảng `order_items`

Bảng `order_items` lưu các sản phẩm thuộc một đơn hàng.

| Cột | Kiểu dữ liệu tham khảo | Ý nghĩa |
|---|---|---|
| id | BIGINT | Khóa chính của dòng sản phẩm |
| order_id | BIGINT | Mã đơn hàng |
| product_id | BIGINT | Mã sản phẩm gốc |
| product_name | VARCHAR | Tên sản phẩm tại thời điểm đặt hàng |
| price | DECIMAL | Giá sản phẩm tại thời điểm đặt hàng |
| quantity | INT | Số lượng sản phẩm |

## 3. Quan hệ giữa các bảng

- Một bản ghi trong `orders` có thể có nhiều bản ghi trong `order_items`.
- Quan hệ giữa `orders` và `order_items` là **1-N**.
- `order_items.order_id` tham chiếu đến `orders.id`.
- `product_id` được lưu để xác định sản phẩm, còn `product_name` và `price` được lưu dưới dạng snapshot nhằm giữ lại thông tin tại thời điểm đặt hàng.

## 4. Mô hình quan hệ

```text
orders
------
PK id
customer_id
shipping_address
phone
payment_method
status
total_amount
created_at
updated_at
      |
      | 1 - N
      |
order_items
-----------
PK id
FK order_id
product_id
product_name
price
quantity
```

## 5. Quy tắc dữ liệu

- Mỗi đơn hàng phải có ít nhất một sản phẩm.
- `quantity` phải lớn hơn 0.
- `total_amount` không được âm.
- `shipping_address` không được rỗng.
- `status` chỉ nhận các giá trị:
  - `PENDING`
  - `CONFIRMED`
  - `SHIPPING`
  - `COMPLETED`
  - `CANCELLED`
- Không nên lưu mật khẩu hoặc JWT secret trong cơ sở dữ liệu của Order Service.
- Cấu trúc cột cuối cùng phải được đối chiếu với các Entity và Migration/SQL thực tế của dự án.
