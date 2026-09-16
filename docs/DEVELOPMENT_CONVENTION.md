# DEVELOPMENT CONVENTION

## 1. Quy tắc Git

- Mỗi thành viên làm việc trên một branch riêng.
- TV4 sử dụng branch:

```text
feature/order-service
```

- Không push trực tiếp lên `main`.
- Chỉ commit những thay đổi có liên quan đến công việc đang thực hiện.
- Không commit thư mục build, thư mục dependency hoặc thông tin bí mật.

## 2. Quy tắc đặt tên branch

| Loại công việc | Quy tắc |
|---|---|
| Tính năng mới | `feature/<ten-tinh-nang>` |
| Sửa lỗi | `fix/<ten-loi>` |
| Tài liệu | `docs/<ten-tai-lieu>` |
| Cải thiện mã nguồn | `refactor/<ten-noi-dung>` |

Ví dụ:

```text
feature/order-service
fix/order-validation
docs/api-contract
```

## 3. Quy tắc commit

Commit cần ngắn gọn, mô tả đúng thay đổi thực tế.

Một số mẫu commit:

```text
chore: initialize smart home store repository
docs: add project overview
docs: add order service API contract
docs: document order database design
feat: implement order creation
feat: add customer order history
feat: add order cancellation
feat: add admin order status update
fix: validate order request data
test: verify order service APIs
```

Không tạo commit giả hoặc commit chỉ để tăng số lượng commit.

## 4. Quy tắc mã nguồn

- Sử dụng tên lớp, phương thức và biến nhất quán trong toàn bộ dự án.
- Các API sử dụng JSON.
- Các API cần xác thực phải kiểm tra JWT.
- CUSTOMER chỉ được truy cập dữ liệu đơn hàng của chính mình.
- ADMIN được phép quản lý các đơn hàng theo nghiệp vụ đã thống nhất.
- Kiểm tra dữ liệu đầu vào trước khi lưu vào cơ sở dữ liệu.
- Không thay đổi tên class, endpoint hoặc cấu trúc dữ liệu của thành viên khác nếu chưa trao đổi.

## 5. Quy tắc kiểm thử

Quy trình kiểm thử đề xuất:

1. Khởi động MySQL và tạo cơ sở dữ liệu cần thiết.
2. Khởi động `order-service`.
3. Kiểm thử API bằng Postman hoặc công cụ tương đương.
4. Kiểm tra mã phản hồi HTTP.
5. Kiểm tra dữ liệu trong cơ sở dữ liệu.
6. Kiểm thử các trường hợp lỗi.
7. Sau khi backend ổn định, mới tích hợp với frontend.
8. Kiểm thử lại thông qua API Gateway.

## 6. Bảo mật

- Không commit mật khẩu MySQL thật.
- Không commit JWT secret thật.
- Không đưa token đăng nhập vào mã nguồn.
- Sử dụng biến môi trường hoặc file cấu hình cục bộ không được commit.
- Kiểm tra kỹ các file `application.properties`, `application.yml` trước khi push lên repository công khai.

## 7. Quy tắc tích hợp

- Frontend gọi API thông qua API Gateway tại port `8080` khi tích hợp.
- Order Service chạy độc lập tại port `8083`.
- Khi thay đổi endpoint, phải thông báo cho các thành viên liên quan.
- Các thay đổi liên quan đến Auth hoặc API Gateway cần được trao đổi với thành viên phụ trách phần đó.
