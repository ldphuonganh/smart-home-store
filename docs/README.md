# SMART HOME STORE

## 1. Giới thiệu dự án

**Smart Home Store** là hệ thống thương mại điện tử hỗ trợ việc kinh doanh và mua bán các sản phẩm nội thất, thiết bị nhà ở thông minh trên nền tảng trực tuyến.

Dự án được xây dựng trong học phần **Phát triển phần mềm hướng dịch vụ**, sử dụng RESTful API và ứng dụng Client.

Các chức năng chính của hệ thống gồm:

- Đăng ký và đăng nhập tài khoản.
- Quản lý tài khoản người dùng.
- Quản lý sản phẩm và danh mục sản phẩm.
- Quản lý giỏ hàng và danh sách yêu thích.
- Đặt hàng và Checkout.
- Theo dõi lịch sử đơn hàng.
- Quản lý đơn hàng dành cho quản trị viên.
- Quản lý thanh toán.
- Cung cấp RESTful API cho ứng dụng Client và các đối tác được cấp quyền.

## 2. Mục tiêu của dự án

Dự án được thực hiện nhằm xây dựng một hệ thống thương mại điện tử có khả năng hỗ trợ các hoạt động mua bán sản phẩm nội thất và thiết bị nhà ở thông minh.

Các mục tiêu chính bao gồm:

- Xây dựng hệ thống thương mại điện tử có giao diện thân thiện với người dùng.
- Áp dụng kiến trúc hướng dịch vụ trong quá trình phát triển phần mềm.
- Xây dựng các RESTful API phục vụ cho ứng dụng Client.
- Phân chia hệ thống thành các service theo từng nhóm chức năng nghiệp vụ.
- Đảm bảo khả năng mở rộng và bảo trì hệ thống.
- Áp dụng quy trình làm việc nhóm thông qua Git và GitHub.
- Kiểm thử độc lập từng chức năng trước khi tích hợp toàn hệ thống.

## 3. Thông tin dự án

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

## 4. Kiến trúc hệ thống

Hệ thống được tổ chức thành các service theo từng nhóm chức năng nghiệp vụ. Người dùng tương tác với hệ thống thông qua ứng dụng Client. Các request từ Frontend được chuyển tiếp thông qua API Gateway đến service tương ứng.

Kiến trúc tổng quát của hệ thống được mô tả như sau:

```text
                         SMART HOME CLIENT
                       React + TypeScript
                            Port 5173
                                |
                                v
                           API GATEWAY
                            Port 8080
                                |
        ---------------------------------------------------------
        |                |                |                     |
        v                v                v                     v
  AUTH SERVICE     PRODUCT SERVICE    CART SERVICE       ORDER SERVICE
    Port 8081         Port 8082         Port 8084           Port 8083
        |                |                |                     |
        v                v                v                     v
     auth_db         product_db        cart_db              order_db


                                |
                                v
                         PAYMENT SERVICE
                            Port 8085
                                |
                                v
                           payment_db

5. Cấu trúc thư mục dự án
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
6. Phân công chức năng của các thành viên
| Thành viên | Chức năng phụ trách             | Service                          |
| ---------- | ------------------------------- | -------------------------------- |
| TV1        | Auth, Account và Partner        | `auth-service`                   |
| TV2        | Product, Category và Public API | `product-service`                |
| TV3        | Cart và Wishlist                | `cart-service`                   |
| TV4        | Order và Checkout               | `order-service`                  |
| TV5        | Payment và API Gateway          | `payment-service`, `api-gateway` |

7. Các cổng dịch vụ
| Thành phần      | Port |
| --------------- | ---: |
| API Gateway     | 8080 |
| Auth Service    | 8081 |
| Product Service | 8082 |
| Order Service   | 8083 |
| Cart Service    | 8084 |
| Payment Service | 8085 |
| Frontend        | 5173 |

8. Nguyên tắc phát triển dự án

Trong quá trình phát triển, nhóm thống nhất áp dụng các nguyên tắc sau:

Mỗi thành viên làm việc trên một branch riêng.
Không đẩy mã nguồn trực tiếp lên branch main.
Mỗi thành viên phải có các commit thể hiện rõ phần công việc đã thực hiện.
Tên branch cần thể hiện rõ chức năng hoặc nhiệm vụ được phụ trách.
Commit cần có nội dung ngắn gọn, dễ hiểu và phản ánh đúng thay đổi.
Backend cần được kiểm thử trước khi tích hợp với Frontend.
Frontend gọi API thông qua API Gateway thay vì gọi trực tiếp đến các service.
Mỗi service chỉ truy cập cơ sở dữ liệu của chính service đó.
Các service giao tiếp với nhau thông qua API.
Hạn chế chỉnh sửa các file thuộc phạm vi phụ trách của thành viên khác.
Khi cần tích hợp mã nguồn, các thành viên sử dụng Pull Request.
Không đưa mật khẩu cơ sở dữ liệu, JWT Secret, API Key hoặc các thông tin nhạy cảm lên repository công khai.
Cần kiểm tra mã nguồn trước khi commit và push lên GitHub.
9. Quy trình làm việc với Git và GitHub

Quy trình làm việc cơ bản của mỗi thành viên gồm các bước:

Cập nhật mã nguồn mới nhất từ branch main.
Chuyển sang branch cá nhân của mình.
Thực hiện phát triển chức năng được phân công.
Kiểm thử chức năng trên máy cá nhân.
Commit các thay đổi lên branch cá nhân.
Push branch lên repository GitHub.
Tạo Pull Request để đề xuất tích hợp mã nguồn vào branch main.
Các thành viên kiểm tra thay đổi trước khi tiến hành merge.

Mỗi thành viên cần đảm bảo phần đóng góp của mình được thể hiện thông qua lịch sử commit trên GitHub.

10. Thứ tự khởi động hệ thống

Thứ tự khởi động hệ thống được đề xuất như sau:

Auth Service.
Product Service.
Cart Service.
Order Service.
Payment Service.
API Gateway.
Frontend.

Trong quá trình phát triển, các service chưa hoàn thành có thể được chạy độc lập để phục vụ việc kiểm thử từng chức năng.

Sau khi các service được hoàn thiện, nhóm tiến hành khởi động toàn bộ hệ thống để kiểm thử tích hợp.

11. Kiểm thử hệ thống

Nhóm dự kiến sử dụng các công cụ và phương pháp sau để kiểm thử hệ thống:

Kiểm thử RESTful API bằng Postman.
Kiểm tra mã trạng thái HTTP.
Kiểm tra dữ liệu đầu vào và dữ liệu trả về.
Kiểm tra chức năng đăng ký và đăng nhập.
Kiểm tra xác thực và phân quyền người dùng.
Kiểm tra các trường hợp dữ liệu hợp lệ và không hợp lệ.
Kiểm tra các trường hợp thiếu dữ liệu bắt buộc.
Kiểm tra xử lý lỗi của từng service.
Kiểm thử tích hợp giữa các service.
Kiểm thử chức năng Frontend.
Kiểm thử End-to-End sau khi hoàn thành quá trình tích hợp.

12. Tài liệu dự án

Thư mục docs/ chứa các tài liệu dùng chung của nhóm.
| Tài liệu                    | Nội dung                                             |
| --------------------------- | ---------------------------------------------------- |
| `README.md`                 | Thông tin tổng quan và hướng dẫn sử dụng dự án       |
| `API_CONTRACT.md`           | Quy ước API, request, response và mã trạng thái HTTP |
| `DATABASE_DESIGN.md`        | Thiết kế cơ sở dữ liệu của hệ thống                  |
| `DEVELOPMENT_CONVENTION.md` | Quy tắc code, branch, commit và quy trình phát triển |
13. Trạng thái phát triển dự án

Dự án được triển khai theo các giai đoạn sau:

Phân tích yêu cầu nghiệp vụ.
Xác định chức năng của từng thành viên.
Thiết kế kiến trúc hệ thống.
Thiết kế cơ sở dữ liệu.
Thống nhất quy ước API và quy tắc phát triển.
Phát triển các service theo chức năng được phân công.
Kiểm thử độc lập từng service.
Phát triển và kết nối Frontend.
Tích hợp các branch thông qua Pull Request.
Kết nối các service thông qua API Gateway.
Kiểm thử tích hợp toàn hệ thống.
Hoàn thiện tài liệu và chuẩn bị báo cáo.
14. Định hướng phát triển

Trong các giai đoạn tiếp theo, nhóm sẽ tiếp tục:

Hoàn thiện các service theo chức năng được phân công.
Bổ sung các chức năng còn thiếu của hệ thống.
Hoàn thiện giao diện ứng dụng Client.
Tăng cường kiểm thử API và kiểm thử tích hợp.
Cải thiện khả năng xử lý lỗi.
Hoàn thiện cơ chế xác thực và phân quyền.
Bổ sung tài liệu hướng dẫn cài đặt và sử dụng.
Đánh giá và cải thiện hiệu năng hệ thống.
Chuẩn bị nội dung báo cáo, slide và phần trình diễn sản phẩm.
