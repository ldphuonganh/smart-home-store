# DATABASE DESIGN – PRODUCT SERVICE

Database: `product_db` (MySQL, tự tạo khi chạy nhờ `createDatabaseIfNotExist=true`). Bảng được Hibernate tạo/cập nhật (`ddl-auto=update`).

Chỉ product-service được truy cập `product_db`. Service khác lấy dữ liệu sản phẩm qua REST API (mục 6 của API_CONTRACT).

## 1. Bảng `categories`

| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Mã danh mục |
| name | VARCHAR(100) | NOT NULL, UNIQUE | Tên danh mục |
| description | VARCHAR(500) | NULL | Mô tả |
| created_at | DATETIME(6) | NOT NULL | Thời điểm tạo |
| updated_at | DATETIME(6) | NOT NULL | Thời điểm cập nhật |

## 2. Bảng `products`

| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Mã sản phẩm (`productId` ở các service khác) |
| name | VARCHAR(150) | NOT NULL, UNIQUE | Tên sản phẩm |
| price | DECIMAL(15,0) | NOT NULL, ≥ 0 | Giá bán (VNĐ, số nguyên) |
| description | TEXT | NULL | Mô tả |
| image_url | VARCHAR(500) | NULL | `/api/products/images/{file}` hoặc link ngoài |
| stock | INT | NOT NULL, ≥ 0, mặc định 0 | Tồn kho |
| category_id | BIGINT | FK → categories.id | Danh mục |
| created_at | DATETIME(6) | NOT NULL | Thời điểm tạo |
| updated_at | DATETIME(6) | NOT NULL | Thời điểm cập nhật |

## 3. Quan hệ

```text
categories (1) ───< (N) products
   PK id                FK category_id
```

- Quan hệ chỉ khai báo 1 chiều trong code (`Product.category`). Entity `Category` không giữ `List<Product>` để tránh vòng lặp JSON/`toString` và tránh xoá dây chuyền.
- **Không cascade xoá**: xoá danh mục còn sản phẩm trả `409`.

## 4. Quy tắc dữ liệu

- Tên sản phẩm và tên danh mục không trùng (so sánh không phân biệt hoa thường).
- `price` dùng `BigDecimal` (không dùng `Double`) để không sai số khi tính tiền.
- `stock` chỉ giảm qua `reserve-stock` bằng câu `UPDATE ... WHERE stock >= :quantity` (không bao giờ âm).
- Ảnh tải lên lưu trong thư mục `product-service/uploads/` (đã `.gitignore`), DB chỉ lưu đường dẫn.

## 5. Dữ liệu mẫu

Khi `product_db` còn trống, `DataSeeder` tạo 4 danh mục và 9 sản phẩm nhà thông minh (trong đó có 1 sản phẩm hết hàng để test). Muốn tạo lại, xoá dữ liệu 2 bảng rồi chạy lại service.
