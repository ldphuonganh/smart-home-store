# API CONTRACT – PRODUCT SERVICE (TV2)

> Service: `product-service` · Port: `8082` · Database: `product_db` · Branch: `feature/product-service`

## 1. Quy ước chung

| Nội dung | Giá trị |
|---|---|
| Client gọi qua Gateway | `http://localhost:8080/api/...` |
| Test độc lập (Postman) | `http://localhost:8082/...` (bỏ tiền tố `/api`) |
| Customer / Admin | `Authorization: Bearer <JWT>` (JWT do auth-service cấp, claim `role` = `CUSTOMER` / `ADMIN`) |
| Partner | `X-API-KEY: <API_KEY>`, scope `products:read` (Gateway kiểm tra) |
| JSON | camelCase, tiền là `number` (VNĐ), thời gian ISO-8601 |

### Rewrite tại Gateway

| Client gọi | Gateway chuyển tới product-service |
|---|---|
| `/api/products/**` | `/products/**` |
| `/api/categories/**` | `/categories/**` |
| `/api/public/products/**` | `/public/products/**` |
| *(không định tuyến)* | `/internal/products/**` – chỉ service nội bộ gọi thẳng cổng 8082 |

### Format lỗi (thống nhất cả nhóm)

```json
{
  "timestamp": "2026-09-29T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Tên sản phẩm đã tồn tại",
  "path": "/products"
}
```

Lỗi validation (400) có thêm `errors` để Frontend hiển thị dưới từng ô:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Tên sản phẩm không được để trống",
  "path": "/products",
  "errors": { "name": "Tên sản phẩm không được để trống", "price": "Giá phải lớn hơn hoặc bằng 0" }
}
```

| Status | Khi nào |
|---:|---|
| 200 | Thành công |
| 201 | Tạo mới thành công |
| 204 | Xoá thành công |
| 400 | Dữ liệu sai / thiếu, `minPrice > maxPrice`, sort theo trường không tồn tại, file không phải ảnh |
| 401 | Thiếu token hoặc token sai/hết hạn khi gọi API cần đăng nhập |
| 403 | Có token nhưng không phải ADMIN |
| 404 | Không tìm thấy sản phẩm / danh mục / ảnh |
| 409 | Trùng tên, không đủ hàng, xoá danh mục còn sản phẩm |

## 2. Đối tượng dữ liệu

### ProductResponse

```json
{
  "id": 1,
  "name": "Khoá cửa vân tay",
  "price": 3490000,
  "description": "Mở khoá bằng vân tay, mật mã, thẻ từ và app.",
  "imageUrl": "/api/products/images/4f1c...e2.png",
  "stock": 15,
  "categoryId": 2,
  "categoryName": "An ninh",
  "createdAt": "2026-09-29T10:00:00",
  "updatedAt": "2026-09-29T10:00:00"
}
```

`imageUrl` là đường dẫn tương đối qua Gateway (Frontend ghép với `VITE_API_BASE_URL`), hoặc link ảnh ngoài (`https://...`).

### ProductRequest (ADMIN thêm/sửa)

| Trường | Kiểu | Bắt buộc | Ràng buộc |
|---|---|---|---|
| name | string | Có | ≤ 150 ký tự, không trùng (không phân biệt hoa thường) |
| price | number | Có | ≥ 0, số nguyên (VNĐ) |
| stock | integer | Có | ≥ 0 |
| categoryId | number | Có | Danh mục phải tồn tại (404 nếu không) |
| description | string | Không | ≤ 5000 ký tự |
| imageUrl | string | Không | Link ảnh ngoài; ảnh tải lên dùng API riêng |

### PageResponse&lt;T&gt;

```json
{ "content": [ ... ], "page": 0, "size": 12, "totalElements": 9, "totalPages": 1, "first": true, "last": true }
```

### CategoryResponse / CategoryRequest

```json
{ "id": 2, "name": "An ninh", "description": "Camera, khoá cửa, cảm biến", "productCount": 3 }
```

Request: `{ "name": "An ninh", "description": "..." }` – `name` bắt buộc, ≤ 100 ký tự, không trùng.

## 3. Product API (Client)

### 3.1. `GET /api/products` – Tìm kiếm, lọc, phân trang · Public

| Query | Kiểu | Mặc định | Ý nghĩa |
|---|---|---|---|
| keyword | string | – | Tên chứa từ khoá (không phân biệt hoa thường) |
| categoryId | number | – | Lọc theo danh mục |
| minPrice / maxPrice | number | – | Khoảng giá (400 nếu min > max) |
| inStock | boolean | – | `true` = chỉ sản phẩm còn hàng |
| page | int | 0 | Trang, bắt đầu từ 0 |
| size | int | 12 | Số phần tử/trang |
| sort | string | `id,desc` | `price,asc`, `price,desc`, `name,asc`, `createdAt,desc`... |

Ví dụ: `GET /api/products?keyword=đèn&categoryId=1&minPrice=100000&maxPrice=500000&inStock=true&sort=price,asc`

→ `200 OK` + `PageResponse<ProductResponse>`

### 3.2. `GET /api/products/{id}` · Public

→ `200` ProductResponse · `404` nếu không tồn tại

### 3.3. `POST /api/products` · ADMIN

Body: ProductRequest → `201` ProductResponse · `400` · `401` · `403` · `404` (categoryId) · `409` (trùng tên)

### 3.4. `PUT /api/products/{id}` · ADMIN

Body: ProductRequest (gửi đủ các trường) → `200` · `400` · `401` · `403` · `404` · `409`

### 3.5. `DELETE /api/products/{id}` · ADMIN

→ `204` · `401` · `403` · `404`. Đơn hàng cũ không bị ảnh hưởng vì order-service lưu snapshot tên và giá.

### 3.6. `POST /api/products/{id}/image` · ADMIN

`multipart/form-data`, field `file` (jpg, jpeg, png, webp, gif; tối đa 5MB) → `200` ProductResponse với `imageUrl` mới · `400` nếu không phải ảnh / quá dung lượng. Ảnh cũ do service lưu sẽ bị xoá.

### 3.7. `GET /api/products/images/{fileName}` · Public

Trả về file ảnh (`image/*`) · `404` nếu không có.

## 4. Category API

| Method | URL | Quyền | Kết quả |
|---|---|---|---|
| GET | `/api/categories` | Public | `200` `CategoryResponse[]` |
| GET | `/api/categories/{id}` | Public | `200` · `404` |
| POST | `/api/categories` | ADMIN | `201` · `400` · `409` trùng tên |
| PUT | `/api/categories/{id}` | ADMIN | `200` · `400` · `404` · `409` |
| DELETE | `/api/categories/{id}` | ADMIN | `204` · `404` · `409` nếu danh mục còn sản phẩm |

Lấy sản phẩm theo danh mục: `GET /api/products?categoryId={id}`.

## 5. Public Product API (Partner)

| Method | URL | Header |
|---|---|---|
| GET | `/api/public/products` | `X-API-KEY` có scope `products:read` |
| GET | `/api/public/products/{id}` | như trên |

Hỗ trợ cùng tham số lọc như 3.1 (mặc định `size=20`, `sort=id,asc`). Chỉ đọc – Partner không thêm/sửa/xoá được.
Gateway trả `403` nếu thiếu/sai/hết hạn/bị thu hồi API Key hoặc thiếu scope.

## 6. API nội bộ (Internal) – dành cho order-service / cart-service

Gọi thẳng `http://localhost:8082` (cấu hình `product-service.base-url`), **không** đi qua Gateway.

### 6.1. `GET /internal/products/{id}`

→ `200` ProductResponse (có `name`, `price`, `stock` để order-service lưu snapshot) · `404`

### 6.2. `PATCH /internal/products/{id}/reserve-stock`

Body: `{ "quantity": 2 }` (≥ 1)

- `200` ProductResponse với `stock` đã trừ
- `409` nếu không đủ hàng: `"Sản phẩm 'Khoá cửa vân tay' không đủ hàng (còn 1, cần 2)"`
- `404` sản phẩm không tồn tại · `400` quantity < 1

Trừ kho bằng **một câu UPDATE có điều kiện** `stock >= quantity`, nên nhiều đơn đặt cùng lúc cũng không làm kho âm.

### 6.3. `PATCH /internal/products/{id}/release-stock`

Body: `{ "quantity": 2 }` → `200` ProductResponse với `stock` đã cộng lại · `404` · `400`

### 6.4. Luồng đề xuất cho order-service (TV4)

```text
POST /api/orders
  for từng item:
      GET  /internal/products/{id}               -> lấy tên, giá thật (không tin giá client gửi)
      PATCH /internal/products/{id}/reserve-stock -> 409 thì DỪNG
  nếu một item thất bại -> PATCH release-stock cho các item đã trừ trước đó (bù trừ)
  lưu Order (snapshot productName, price)

PUT /api/orders/{id}/cancel
  PATCH /internal/products/{id}/release-stock cho từng item
```

**Giới hạn đã biết** (giống Buổi 3/10): không có transaction phân tán giữa `order_db` và `product_db`. Nếu order-service chết giữa chừng sau khi đã trừ kho, tồn kho có thể lệch. Không làm Saga/Outbox trong phạm vi đồ án. `/internal/**` hiện được `permitAll` và dựa vào việc Gateway không định tuyến ra ngoài.
