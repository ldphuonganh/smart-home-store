# SMART HOME STORE

## 1. Tổng quan

**Smart Home Store** là hệ thống thương mại điện tử được xây dựng trong học phần **Phát triển phần mềm hướng dịch vụ**. Hệ thống hỗ trợ khách hàng tìm kiếm sản phẩm, quản lý giỏ hàng, wishlist, checkout, đặt hàng và thanh toán; đồng thời cung cấp các chức năng quản trị cho tài khoản, sản phẩm, danh mục, đơn hàng, khuyến mãi và thanh toán.

Tên đề tài thống nhất của nhóm:

> **Thiết kế và phát triển hệ thống thương mại điện tử Smarthome dựa trên RESTful API và ứng dụng Client**

Hệ thống sử dụng các service độc lập theo domain nghiệp vụ, giao tiếp chủ yếu bằng RESTful API và sử dụng API Gateway làm điểm truy cập chung cho Frontend.

## 2. Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Frontend | React + TypeScript + Vite |
| Backend | Java + Spring Boot |
| API Gateway | Spring Cloud Gateway |
| Database | MySQL |
| Giao tiếp | HTTP/REST + JSON |
| Authentication | JWT |
| API kiểm thử | Postman |
| Frontend port | 5173 |
| Gateway port | 8080 |

## 3. Kiến trúc tổng thể

```text
                         React + TypeScript
                              :5173
                                |
                              REST
                                |
                                v
                       +------------------+
                       |   API Gateway    |
                       |      :8080       |
                       +------------------+
                         /    |    |    \
                        /     |    |     \
                       v      v    v      v
                    Auth   Product Cart   Order
                    :8081   :8082  :8084  :8083
                      |       |      |       |
                   auth_db product cart_db order_db
                              db

                              |
                              v
                         Payment
                          :8085
                            |
                       payment_db
```

### Nguyên tắc kiến trúc

- Mỗi service chịu trách nhiệm một domain nghiệp vụ riêng.
- Mỗi service sở hữu database của mình.
- Service không truy cập trực tiếp database của service khác.
- Khi cần dữ liệu hoặc nghiệp vụ của service khác, service gọi API tương ứng.
- Frontend gọi API thông qua Gateway thay vì gọi trực tiếp từng service.
- Các API yêu cầu đăng nhập sử dụng JWT để xác thực và phân quyền.
- Các API nội bộ giữa service không được công khai qua Gateway nếu không cần thiết.

## 4. Các service

| Service | Port | Database | Trách nhiệm |
|---|---:|---|---|
| `api-gateway` | 8080 | - | Routing, CORS và điểm vào chung |
| `auth-service` | 8081 | `auth_db` | Đăng ký, đăng nhập, tài khoản, JWT, role, địa chỉ, API Key |
| `product-service` | 8082 | `product_db` | Sản phẩm, danh mục, tồn kho, hình ảnh, đánh giá, Public Product API |
| `order-service` | 8083 | `order_db` | Order, Checkout, khuyến mãi, vận chuyển |
| `cart-service` | 8084 | `cart_db` | Giỏ hàng và Wishlist |
| `payment-service` | 8085 | `payment_db` | Giao dịch QR, VNPay, PayPal và đối soát thanh toán |
| `crs-frontend` | 5173 | - | Giao diện Client |

## 5. Chức năng chính

### Customer

- Đăng ký, đăng nhập và quản lý tài khoản.
- Quản lý địa chỉ giao hàng.
- Xem, tìm kiếm và lọc sản phẩm.
- Xem danh mục, thương hiệu và sản phẩm nổi bật.
- Xem chi tiết sản phẩm.
- Quản lý giỏ hàng.
- Chọn/bỏ chọn sản phẩm trong giỏ.
- Quản lý Wishlist.
- Checkout và tính báo giá.
- Đặt hàng.
- Xem lịch sử và chi tiết đơn hàng.
- Hủy/chỉnh sửa đơn theo trạng thái cho phép.
- Theo dõi đơn bằng mã đơn và số điện thoại.
- Thanh toán theo phương thức được hệ thống hỗ trợ.
- Xem và gửi đánh giá sản phẩm khi đủ điều kiện.

### Admin

- Quản lý tài khoản và trạng thái người dùng.
- Quản lý role.
- Quản lý sản phẩm, danh mục và hình ảnh.
- Quản lý đánh giá.
- Quản lý đơn hàng và trạng thái đơn.
- Xác nhận thanh toán/hoàn tiền theo nghiệp vụ.
- Quản lý mã giảm giá.
- Theo dõi các giao dịch thanh toán.
- Quản lý API Key/Partner theo phạm vi được triển khai.

## 6. Giao tiếp giữa các service

Ví dụ luồng Checkout:

```text
Customer
   |
   v
Frontend :5173
   |
   v
Gateway :8080
   |
   v
Order Service :8083
   |
   | REST API
   v
Product Service :8082
   |
   v
product_db

Nếu giữ tồn kho thành công:
   |
   v
Order Service tạo Order + OrderItem
   |
   v
order_db

Nếu tạo đơn thất bại sau khi đã giữ kho:
   |
   v
Order Service gọi Product Service
release-stock
```

`Order Service` không truy cập `product_db` trực tiếp. `Product Service` là service sở hữu thông tin sản phẩm và tồn kho.

## 7. Phân công

| Thành viên | Phạm vi |
|---|---|
| TV1 | Auth, Account, Partner/API Key |
| TV2 | Product, Category, Public Product API |
| TV3 | Cart, Wishlist |
| TV4 | Order, Checkout |
| TV5 | Payment, API Gateway |

## 8. Git/GitHub

Nhóm sử dụng branch riêng cho từng phần chức năng và tích hợp thông qua Pull Request.

Branch của phần Order + Checkout:

```text
feature/order-service
```

Quy trình:

```text
Branch cá nhân
    ↓
Phát triển
    ↓
Test
    ↓
Commit
    ↓
Push
    ↓
Pull Request
    ↓
Review
    ↓
Merge
```

## 9. Kiểm thử

Hệ thống được kiểm thử ở nhiều mức:

- Kiểm thử API bằng Postman.
- Kiểm tra HTTP status code.
- Kiểm tra validation dữ liệu.
- Kiểm tra authentication/authorization.
- Kiểm tra xử lý lỗi.
- Kiểm thử từng service độc lập.
- Kiểm thử giao tiếp giữa các service.
- Kiểm thử Frontend.
- Kiểm thử tích hợp qua API Gateway.
- Kiểm thử các luồng nghiệp vụ chính End-to-End.

## 10. Các điểm kỹ thuật đáng chú ý

### Ownership dữ liệu

Mỗi service sở hữu dữ liệu thuộc domain của mình. Service khác sử dụng API thay vì truy cập database trực tiếp.

### Snapshot OrderItem

Khi đặt hàng, `OrderItem` lưu tên, giá và thông tin sản phẩm tại thời điểm đặt hàng. Vì vậy thay đổi giá sản phẩm sau này không làm thay đổi lịch sử đơn hàng cũ.

### Kiểm soát tồn kho

Product Service xử lý nghiệp vụ giữ/trả tồn kho. Việc cập nhật tồn kho được thực hiện ở Product Service để tránh việc Order Service tự thao tác vào `product_db`.

### IDOR

Khi khách xem đơn hàng, backend lấy người dùng từ JWT và kiểm tra quyền sở hữu đơn hàng trước khi trả dữ liệu.

### Distributed consistency

Order, Product và Payment sử dụng các database khác nhau. Vì vậy các nghiệp vụ đi qua nhiều service cần xử lý lỗi, timeout và compensation phù hợp. Hệ thống hiện có xử lý hoàn tồn kho trong một số trường hợp, nhưng chưa phải một Saga hoàn chỉnh.

### Idempotency

Các nghiệp vụ tạo đơn cần tránh việc cùng một yêu cầu được xử lý nhiều lần. Đây là một điểm cần tiếp tục tăng cường nếu hệ thống được phát triển thêm.

## 11. Trạng thái hoàn thành

Các thành phần chính của hệ thống đã được triển khai và tích hợp:

```text
Auth
  ↓
Product
  ↓
Cart / Wishlist
  ↓
Order / Checkout
  ↓
Payment
  ↓
API Gateway
  ↓
React Client
```

Dự án hiện ở giai đoạn **hoàn thiện và tích hợp hệ thống**, sẵn sàng cho kiểm thử tổng thể, báo cáo và trình diễn.

## 12. Định hướng cải tiến

Một số hướng có thể tiếp tục phát triển:

- Hoàn thiện cơ chế idempotency cho các thao tác quan trọng.
- Tăng cường timeout/retry giữa các service.
- Áp dụng Saga/Compensating Transaction cho các nghiệp vụ phân tán phức tạp.
- Bổ sung kiểm thử concurrency cho tồn kho.
- Hoàn thiện monitoring và logging tập trung.
- Bổ sung CI/CD và kiểm thử tự động.
