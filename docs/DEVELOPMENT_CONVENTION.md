# DEVELOPMENT CONVENTION

## 1. Mục đích

Tài liệu này thống nhất cách làm việc với mã nguồn, Git/GitHub, API, database và quá trình tích hợp của nhóm Smart Home Store.

## 2. Branch

Mỗi thành viên phát triển trên branch riêng.

Các branch chính theo phân công:

```text
feature/auth-api-key
feature/product-service
feature/cart-service
feature/order-service
```

Phần Payment và API Gateway do TV5 phụ trách theo phân công của nhóm.

Không push trực tiếp mã nguồn chức năng lên `main` khi đang phát triển.

## 3. Quy tắc commit

Commit phải mô tả đúng thay đổi.

Ví dụ:

```text
feat(order): cap nhat xu ly checkout
feat(order): xu ly hoan kho khi dat hang loi
test(order): them test checkout
fix(order): kiem tra quyen xem don
docs: cap nhat tai lieu api
```

Nguyên tắc:

- Commit nhỏ, có ý nghĩa.
- Không tạo commit giả chỉ để tăng số lượng.
- Không commit file build hoặc dependency không cần thiết.
- Không commit secret, token hoặc thông tin nhạy cảm.

## 4. Pull Request

Quy trình tích hợp:

```text
Feature Branch
      ↓
Development
      ↓
Local Test
      ↓
Push
      ↓
Pull Request
      ↓
Review
      ↓
Merge
      ↓
Integration Test
```

Trước khi tạo Pull Request cần kiểm tra:

- Code compile.
- API chạy được.
- Không có file không liên quan.
- Không chứa secret.
- Chức năng phụ trách đã được kiểm thử.

## 5. Quy tắc API

- Sử dụng HTTP method đúng mục đích.
- Request/response sử dụng JSON khi phù hợp.
- API cần đăng nhập sử dụng JWT.
- Endpoint phải có tên nhất quán.
- Khi thay đổi API phải cập nhật tài liệu.
- Frontend gọi API qua Gateway.
- Không để Frontend phụ thuộc trực tiếp vào địa chỉ của từng service.

## 6. Quy tắc service

Mỗi service có business responsibility riêng:

```text
Auth       -> tài khoản/xác thực
Product    -> sản phẩm/tồn kho
Cart       -> giỏ hàng/Wishlist
Order      -> Order/Checkout
Payment    -> giao dịch thanh toán
Gateway    -> routing
```

Service không truy cập trực tiếp database của service khác.

Ví dụ:

```text
Order Service
      |
      X
      |
  product_db

Order Service
      |
      v
Product API
      |
      v
product_db
```

## 7. Quy tắc bảo mật

Không commit:

- Mật khẩu MySQL thật.
- JWT secret thật.
- API Key thật.
- Access token.
- Refresh token.
- Thông tin tài khoản bên thứ ba.
- File `.env` chứa secret.

Cần kiểm tra:

```text
application.properties
application.yml
.env
```

trước khi push repository.

## 8. Authentication và Authorization

### Authentication

Xác định người dùng là ai.

Ví dụ:

```text
Authorization: Bearer <JWT>
```

### Authorization

Xác định người dùng có quyền gì.

Ví dụ:

```text
CUSTOMER
ADMIN
```

Order Service phải kiểm tra ownership khi CUSTOMER truy cập đơn hàng.

## 9. Validation và xử lý lỗi

Input từ Client phải được kiểm tra trước khi thực hiện nghiệp vụ.

Các lỗi nghiệp vụ cần trả về HTTP status phù hợp, ví dụ:

```text
400 -> dữ liệu không hợp lệ
401 -> chưa đăng nhập
403 -> không có quyền
404 -> không tìm thấy
409 -> xung đột nghiệp vụ
500 -> lỗi máy chủ
```

Không nên trả về thông tin nội bộ của hệ thống cho Client.

## 10. Kiểm thử

### API

Sử dụng Postman hoặc công cụ tương đương để kiểm tra:

- HTTP method.
- URL.
- Header.
- JWT.
- Request body.
- Response body.
- HTTP status.

### Service

Mỗi service cần được chạy và kiểm thử độc lập trước khi tích hợp.

### Integration

Sau khi các service hoạt động:

```text
Frontend
   ↓
Gateway
   ↓
Auth / Product / Cart / Order / Payment
   ↓
Database tương ứng
```

cần kiểm thử lại luồng thực tế.

### End-to-End

Các luồng quan trọng:

```text
Register
  ↓
Login
  ↓
Product
  ↓
Cart
  ↓
Checkout
  ↓
Create Order
  ↓
Payment
  ↓
Order Status
```

## 11. Quy tắc cho Order + Checkout

Order Service:

- Lấy người dùng từ JWT.
- Không tin `userId` do Client tự truyền cho nghiệp vụ Order.
- Không truy cập `product_db`.
- Gọi Product Service khi cần thông tin sản phẩm/tồn kho.
- Lưu OrderItem snapshot.
- Khi đã giữ kho nhưng tạo Order thất bại, thực hiện compensation để hoàn kho nếu có thể.
- Kiểm tra ownership khi CUSTOMER truy cập đơn.
- Không cho phép chuyển trạng thái Order tùy ý.

## 12. Quy tắc cho Product + Stock

Product Service là nơi sở hữu tồn kho.

Các thao tác chính:

```text
reserve-stock
release-stock
sold
```

Kiểm soát tồn kho phải được thực hiện ở Product Service để tránh hai request đồng thời cùng sử dụng một lượng hàng cuối.

## 13. Quy tắc Frontend

Frontend:

```text
React + TypeScript + Vite
```

Port:

```text
5173
```

Frontend gọi:

```text
http://localhost:8080
```

thông qua Gateway khi tích hợp.

Không hard-code địa chỉ của từng service trong các luồng nghiệp vụ của Client.

## 14. Quy trình phát triển hoàn chỉnh

```text
Phân tích nghiệp vụ
        ↓
Thiết kế service
        ↓
Thiết kế database
        ↓
Thống nhất API
        ↓
Phát triển backend
        ↓
Test API
        ↓
Phát triển frontend
        ↓
Tích hợp Gateway
        ↓
Integration Test
        ↓
End-to-End Test
        ↓
Fix lỗi
        ↓
Hoàn thiện docs
        ↓
Merge main
```

## 15. Trạng thái hoàn thành

Khi một module được xem là hoàn thành:

- Code đã được commit.
- API đã được kiểm thử.
- Database hoạt động.
- Frontend đã kết nối nếu module có giao diện.
- Không có secret trong repository.
- Tài liệu liên quan đã được cập nhật.
- Branch đã được tích hợp thông qua Pull Request.

Dự án sau khi các module được tích hợp và kiểm thử tổng thể có thể chuyển sang giai đoạn **hoàn thiện báo cáo và trình diễn**.
