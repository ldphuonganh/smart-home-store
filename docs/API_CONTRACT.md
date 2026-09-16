# API CONTRACT

## 1. Quy ước chung

### 1.1. Địa chỉ API

Khi tích hợp hoàn chỉnh, Client gọi API thông qua API Gateway:

```text
http://localhost:8080
```

Trong quá trình phát triển độc lập, Order Service có thể được kiểm thử trực tiếp tại:

```text
http://localhost:8083
```

### 1.2. Header

Đối với request JSON:

```http
Content-Type: application/json
```

Đối với API yêu cầu đăng nhập:

```http
Authorization: Bearer <JWT_TOKEN>
```

### 1.3. Mã phản hồi HTTP

| Mã | Ý nghĩa |
|---:|---|
| 200 | Yêu cầu thành công |
| 201 | Tạo dữ liệu thành công |
| 400 | Dữ liệu đầu vào không hợp lệ |
| 401 | Chưa xác thực hoặc token không hợp lệ |
| 403 | Không có quyền thực hiện |
| 404 | Không tìm thấy dữ liệu |
| 500 | Lỗi máy chủ |

# 2. API của Order Service

## 2.1. Tạo đơn hàng

```http
POST /api/orders
```

**Quyền truy cập:** CUSTOMER đã đăng nhập.

**Request Body mẫu:**

```json
{
  "shippingAddress": "123 Nguyễn Trãi, Quận 1, TP.HCM",
  "phone": "0901234567",
  "paymentMethod": "COD",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

| Trường | Kiểu dữ liệu | Bắt buộc | Mô tả |
|---|---|---|---|
| shippingAddress | String | Có | Địa chỉ giao hàng |
| phone | String | Có | Số điện thoại nhận hàng |
| paymentMethod | String | Có | Phương thức thanh toán |
| items | Array | Có | Danh sách sản phẩm |
| productId | Integer | Có | Mã sản phẩm |
| quantity | Integer | Có | Số lượng sản phẩm |

**Response thành công:** `201 Created`

```json
{
  "id": 1,
  "customerId": 2,
  "shippingAddress": "123 Nguyễn Trãi, Quận 1, TP.HCM",
  "phone": "0901234567",
  "paymentMethod": "COD",
  "status": "PENDING",
  "totalAmount": 25980000,
  "items": [
    {
      "productId": 1,
      "productName": "Smart Sofa",
      "price": 12990000,
      "quantity": 2
    }
  ]
}
```

## 2.2. Xem đơn hàng của khách hàng

```http
GET /api/orders/my
```

**Quyền truy cập:** CUSTOMER đã đăng nhập.

**Response thành công:** `200 OK`

```json
[
  {
    "id": 1,
    "customerId": 2,
    "shippingAddress": "123 Nguyễn Trãi, Quận 1, TP.HCM",
    "phone": "0901234567",
    "paymentMethod": "COD",
    "status": "PENDING",
    "totalAmount": 25980000,
    "items": [
      {
        "productId": 1,
        "productName": "Smart Sofa",
        "price": 12990000,
        "quantity": 2
      }
    ]
  }
]
```

## 2.3. Xem tất cả đơn hàng

```http
GET /api/orders
```

**Quyền truy cập:** ADMIN đã đăng nhập.

**Response thành công:** `200 OK`

```json
[
  {
    "id": 1,
    "customerId": 2,
    "shippingAddress": "123 Nguyễn Trãi, Quận 1, TP.HCM",
    "phone": "0901234567",
    "paymentMethod": "COD",
    "status": "PENDING",
    "totalAmount": 25980000
  }
]
```

## 2.4. Xem chi tiết đơn hàng

```http
GET /api/orders/{id}
```

**Quyền truy cập:**

- CUSTOMER chỉ được xem đơn hàng của mình.
- ADMIN được xem tất cả đơn hàng.

**Ví dụ:**

```http
GET /api/orders/1
```

**Response thành công:** `200 OK`

```json
{
  "id": 1,
  "customerId": 2,
  "shippingAddress": "123 Nguyễn Trãi, Quận 1, TP.HCM",
  "phone": "0901234567",
  "paymentMethod": "COD",
  "status": "PENDING",
  "totalAmount": 25980000,
  "items": [
    {
      "productId": 1,
      "productName": "Smart Sofa",
      "price": 12990000,
      "quantity": 2
    }
  ]
}
```

## 2.5. Hủy đơn hàng

```http
PUT /api/orders/{id}/cancel
```

**Quyền truy cập:** CUSTOMER đã đăng nhập.

**Ví dụ:**

```http
PUT /api/orders/1/cancel
```

Đơn hàng chỉ được hủy khi còn ở trạng thái cho phép hủy, thông thường là `PENDING`. Không cho phép hủy đơn hàng đã `COMPLETED`.

**Response thành công:** `200 OK`

```json
{
  "id": 1,
  "status": "CANCELLED",
  "message": "Hủy đơn hàng thành công"
}
```

## 2.6. Cập nhật trạng thái đơn hàng

```http
PUT /api/orders/{id}/status
```

**Quyền truy cập:** ADMIN đã đăng nhập.

**Request Body:**

```json
{
  "status": "CONFIRMED"
}
```

**Các trạng thái hợp lệ:**

- `PENDING`
- `CONFIRMED`
- `SHIPPING`
- `COMPLETED`
- `CANCELLED`

**Response thành công:** `200 OK`

```json
{
  "id": 1,
  "status": "CONFIRMED",
  "message": "Cập nhật trạng thái đơn hàng thành công"
}
```

# 3. Bảng tổng hợp API

| STT | Phương thức | Endpoint | Đối tượng sử dụng | Chức năng |
|---:|---|---|---|---|
| 1 | POST | `/api/orders` | CUSTOMER | Tạo đơn hàng |
| 2 | GET | `/api/orders/my` | CUSTOMER | Xem đơn hàng của bản thân |
| 3 | GET | `/api/orders` | ADMIN | Xem tất cả đơn hàng |
| 4 | GET | `/api/orders/{id}` | CUSTOMER, ADMIN | Xem chi tiết đơn hàng |
| 5 | PUT | `/api/orders/{id}/cancel` | CUSTOMER | Hủy đơn hàng |
| 6 | PUT | `/api/orders/{id}/status` | ADMIN | Cập nhật trạng thái đơn hàng |

# 4. Quy tắc nghiệp vụ

- Người dùng phải đăng nhập khi tạo hoặc xem đơn hàng cá nhân.
- Đơn hàng phải có ít nhất một sản phẩm.
- Số lượng sản phẩm phải lớn hơn 0.
- Địa chỉ giao hàng không được để trống.
- Tổng tiền được tính dựa trên giá và số lượng sản phẩm.
- Tên sản phẩm và giá sản phẩm được lưu dưới dạng snapshot tại thời điểm đặt hàng.
- CUSTOMER chỉ được xem đơn hàng của chính mình.
- ADMIN có thể xem và quản lý tất cả đơn hàng.
- Trạng thái đơn hàng phải thuộc danh sách trạng thái đã quy định.
- Khi API thay đổi, tài liệu này phải được cập nhật.

> Lưu ý: Endpoint và cấu trúc JSON cần được đối chiếu lại với Controller/DTO thực tế trước khi tích hợp chính thức.
