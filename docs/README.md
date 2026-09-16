# SMART HOME STORE

## 1. Giới thiệu dự án

**Smart Home Store** là hệ thống thương mại điện tử hỗ trợ việc kinh doanh và mua bán các sản phẩm nội thất, thiết bị nhà ở thông minh trên nền tảng trực tuyến.

Các chức năng chính:

- Đăng ký và đăng nhập tài khoản.
- Quản lý tài khoản người dùng.
- Quản lý sản phẩm và danh mục sản phẩm.
- Quản lý giỏ hàng và danh sách yêu thích.
- Đặt hàng và checkout.
- Theo dõi lịch sử đơn hàng.
- Quản lý đơn hàng dành cho quản trị viên.
- Cung cấp RESTful API cho ứng dụng Client và hệ thống bên ngoài.

## 2. Thông tin dự án

| Nội dung | Thông tin |
|---|---|
| Tên dự án | Smart Home Store |
| Tên đề tài | Thiết kế và phát triển hệ thống thương mại điện tử Smarthome dựa trên RESTful API và ứng dụng Client |
| Repository | smart-home-store |
| Nền tảng | Web Application |
| Backend | Java, Spring Boot |
| Frontend | React, TypeScript, Vite |
| Cơ sở dữ liệu | MySQL |
| Giao tiếp | RESTful API |
| API Gateway | Spring Cloud Gateway |

## 3. Cấu trúc thư mục

```text
smart-home-store/
├── api-gateway/
├── auth-service/
├── order-service/
├── crs-frontend/
├── docs/
│   ├── README.md
│   ├── API_CONTRACT.md
│   ├── DATABASE_DESIGN.md
│   └── DEVELOPMENT_CONVENTION.md
└── .gitignore
```

## 4. Phân công chức năng

| Thành viên | Chức năng phụ trách |
|---|---|
| TV1 | Auth, Account và Partner |
| TV2 | Product, Category và Public API |
| TV3 | Cart và Wishlist |
| TV4 | Order và Checkout |
| TV5 | Payment và API Gateway |

## 5. Các cổng dịch vụ

| Thành phần | Port |
|---|---:|
| API Gateway | 8080 |
| Auth Service | 8081 |
| Product Service | 8082 |
| Order Service | 8083 |
| Cart Service | 8084 |
| Payment Service | 8085 |
| Frontend | 5173 |

## 6. Phạm vi công việc của TV4

TV4 phụ trách chức năng quản lý đơn hàng và quy trình checkout:

- Tạo đơn hàng.
- Xem danh sách đơn hàng của khách hàng.
- Xem chi tiết đơn hàng.
- Xem lịch sử đơn hàng.
- Hủy đơn hàng.
- Cập nhật trạng thái đơn hàng.
- Quản lý đơn hàng dành cho ADMIN.
- Lưu thông tin sản phẩm tại thời điểm đặt hàng.
- Lưu thông tin giao hàng và phương thức thanh toán.

## 7. Trạng thái đơn hàng

| Trạng thái | Ý nghĩa |
|---|---|
| PENDING | Đơn hàng đang chờ xử lý |
| CONFIRMED | Đơn hàng đã được xác nhận |
| SHIPPING | Đơn hàng đang được giao |
| COMPLETED | Đơn hàng đã hoàn thành |
| CANCELLED | Đơn hàng đã bị hủy |

## 8. Nguyên tắc phát triển

- Mỗi thành viên làm việc trên một branch riêng.
- Không đẩy mã nguồn trực tiếp lên branch `main`.
- Mỗi thành viên phải có các commit thể hiện phần công việc đã thực hiện.
- Backend cần được kiểm thử trước khi tích hợp frontend.
- Frontend gọi API thông qua API Gateway.
- Không đưa mật khẩu cơ sở dữ liệu, JWT secret hoặc thông tin nhạy cảm lên repository công khai.

## 9. Thứ tự khởi động đề xuất

1. Auth Service.
2. Product Service.
3. Cart Service.
4. Order Service.
5. Payment Service.
6. API Gateway.
7. Frontend.

Các service chưa hoàn thành có thể được chạy riêng trong quá trình phát triển và kiểm thử.
