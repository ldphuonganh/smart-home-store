# product-service (TV2 – Product + Category + Public API)

| | |
|---|---|
| Port | 8082 |
| Database | `product_db` (MySQL, tự tạo) |
| Tài liệu | [API_CONTRACT](docs/API_CONTRACT.md) · [DATABASE_DESIGN](docs/DATABASE_DESIGN.md) · [INTEGRATION_NOTES](docs/INTEGRATION_NOTES.md) |
| Postman | `docs/product-service.postman_collection.json` |

## Chạy

1. Bật MySQL (root / 123456 – sửa trong `src/main/resources/application.properties` nếu khác).
2. Chạy `ProductServiceApplication` trong IntelliJ (hoặc `mvnw spring-boot:run`).
3. Lần đầu chạy với DB trống sẽ tự tạo 4 danh mục + 9 sản phẩm mẫu.

> Nếu trước đây đã chạy bản cũ (price kiểu DOUBLE, chưa có stock), hãy xoá DB cũ trước:
> `DROP DATABASE product_db;` rồi chạy lại service.

## Test

- **Tự động**: chuột phải `src/test/java/.../ProductServiceApplicationTests` → Run (dùng H2 trong bộ nhớ, không cần MySQL). Hoặc `mvnw test`.
- **Postman**: Import `docs/product-service.postman_collection.json`, chạy thư mục 1 → 5 bằng Collection Runner. Token ADMIN/CUSTOMER tự sinh từ `jwtSecret`, không cần bật auth-service.
- **Qua Gateway**: thư mục 6 của collection (cần TV5 sửa filter, xem INTEGRATION_NOTES mục 1).

## Cấu trúc

```text
controller/  ProductController, CategoryController, PublicProductController, InternalProductController
service/     ProductService, CategoryService, FileStorageService
repository/  ProductRepository (+ ProductSpecifications để lọc), CategoryRepository
entity/      Product, Category
dto/         ProductRequest/Response, CategoryRequest/Response, StockRequest, PageResponse, ErrorResponse
exception/   GlobalExceptionHandler + ResourceNotFound(404) / Conflict(409) / BadRequest(400)
security/    JwtAuthFilter (tự verify JWT, không phụ thuộc Gateway)
config/      SecurityConfig, DataSeeder
```
