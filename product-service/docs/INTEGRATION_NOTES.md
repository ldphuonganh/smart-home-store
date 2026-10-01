# GHI CHÚ TÍCH HỢP – PRODUCT SERVICE (TV2 gửi các thành viên)

Theo quy tắc nhóm, TV2 **không tự sửa** service của người khác. Dưới đây là các điểm cần chỉnh để Product tích hợp được, kèm code gợi ý.

## 1. Gửi TV5 – API Gateway (bắt buộc, nếu không Product không chạy được qua 8080)

### 1.1. `AuthHeaderFilter`: mở GET sản phẩm/danh mục và route Partner

Hiện filter chỉ cho qua `GET /api/courses` (còn sót từ CRS), nên khách chưa đăng nhập gọi `GET /api/products` hoặc `GET /api/categories` bị **401**. Route Partner `/api/public/products` cũng bị đòi JWT.

```java
private static final List<String> OPEN_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/public/products"      // Partner dùng X-API-KEY, ApiKeyFilter kiểm tra
);

// thay cho isPublicCourseRead
boolean isPublicRead = HttpMethod.GET.equals(request.getMethod())
        && (path.startsWith("/api/products") || path.startsWith("/api/categories"));

if (isOpen || isPublicRead) {
    return chain.filter(exchange);
}
```

### 1.2. `ApiKeyFilter`: đổi sang Product

```java
private static final String PARTNER_PATH = "/api/public/products";
private static final String REQUIRED_SCOPE = "products:read";
```

Khi tạo API Key cho Partner ở trang Admin, scope nhập là `products:read`.

### 1.3. Routes

`application.yml` hiện có đủ route cho `/api/products/**`, `/api/categories/**`, `/api/public/products/**`, không cần thêm. Ảnh sản phẩm dùng chung route `/api/products/**` (`/api/products/images/{file}`).

**Không** thêm route nào trỏ tới `/internal/**`.

### 1.4. Kiểm tra nhanh sau khi sửa (qua 8080)

| # | Request | Kỳ vọng |
|---|---|---|
| 1 | `GET /api/products` không token | 200 |
| 2 | `GET /api/categories` không token | 200 |
| 3 | `POST /api/products` không token | 401 (chặn tại Gateway) |
| 4 | `POST /api/products` token CUSTOMER | 403 (chặn tại product-service) |
| 5 | `POST /api/products` token ADMIN | 201 |
| 6 | `GET /api/public/products` không `X-API-KEY` | 403 |
| 7 | `GET /api/public/products` `X-API-KEY` đúng scope `products:read` | 200 |

## 2. Gửi TV4 – order-service

`ProductClient` đang gọi `GET /internal/products/{id}` và cần `{id, name, price, stock}`. product-service **đã có** endpoint này, response có đủ các field trên (thêm vài field khác, Jackson tự bỏ qua).

1. Bật kiểm tra sản phẩm thật: `product-service.validation-enabled=true` trong `order-service/application.properties`.
2. Thêm trừ/hoàn kho (mẫu `reserve-seat` Buổi 3):

```java
public void reserveStock(Long productId, int quantity) {
    String url = productServiceBaseUrl + "/internal/products/" + productId + "/reserve-stock";
    try {
        restTemplate.exchange(url, HttpMethod.PATCH,
                new HttpEntity<>(Map.of("quantity", quantity)), Void.class);
    } catch (HttpClientErrorException.Conflict e) {
        throw new IllegalStateException("San pham id = " + productId + " khong du hang");
    } catch (HttpClientErrorException.NotFound e) {
        throw new NoSuchElementException("Khong tim thay san pham id = " + productId);
    } catch (ResourceAccessException e) {
        throw new IllegalStateException("Khong the ket noi toi product-service");
    }
}
// releaseStock tương tự với "/release-stock"
```

> `RestTemplate` mặc định **không hỗ trợ PATCH**. Cần tạo `RestTemplate` với `new HttpComponentsClientHttpRequestFactory()` (thêm dependency `org.apache.httpcomponents.client5:httpclient5`) hoặc dùng `JdkClientHttpRequestFactory` (Spring 6.1+).

3. Trong `createOrder`: trừ kho từng item, item nào lỗi thì `releaseStock` các item đã trừ trước đó rồi ném lỗi. Trong `cancelOrder`: `releaseStock` từng item.
4. Giá và tên luôn lấy từ product-service, **không dùng giá client gửi lên**.

Trang chi tiết sản phẩm của TV2 có nút **"Mua ngay"**, chuyển sang `/checkout` với `location.state.items = [{ productId, productName, price, quantity }]` đúng kiểu `OrderItemRequest` của CheckoutPage.

## 3. Gửi TV3 – cart-service

- Khi thêm vào giỏ nên gọi `GET http://localhost:8082/internal/products/{productId}` để kiểm tra sản phẩm tồn tại, còn hàng (`stock >= quantity`) và lấy giá hiện tại. Không truy cập `product_db`.
- Khi merge, trang chi tiết `pages/products/ProductDetailPage.tsx` có thể thêm nút **"Thêm vào giỏ"** gọi hàm trong `cartApi.ts` của TV3, đặt cạnh nút "Mua ngay".

## 4. Gửi TV1 – auth-service

product-service đọc claim `role` (`ADMIN` / `CUSTOMER`, chấp nhận cả dạng có tiền tố `ROLE_`) và dùng `email` (nếu có, nếu không thì `subject`) làm principal. Secret JWT dùng chung: `jwt.secret` trong `application.properties`.

## 5. Frontend dùng chung

TV2 đã sửa nhỏ các file chung (khi merge cần giữ):

| File | Thay đổi |
|---|---|
| `App.tsx` | Thêm route `/products`, `/products/:id`, `/admin/products`, `/admin/categories`. Trang mặc định `/` và `*` chuyển về `/products` |
| `components/Navbar.tsx` | Thêm link Sản phẩm, Quản lý sản phẩm, Quản lý danh mục |
| `components/ProtectedRoute.tsx` | Sai role chuyển về `/products` (trước đây `/courses` – route không còn tồn tại) |
| `types/apiError.ts` | Khớp format lỗi chung `{timestamp, status, error, message, path, errors}` |
| `components/Pagination.tsx` | Component phân trang dùng chung, các trang khác có thể dùng lại |
