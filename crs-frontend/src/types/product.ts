// Khớp đúng DTO của product-service (ProductResponse, CategoryResponse, PageResponse)

export interface Product {
  id: number;
  name: string;
  price: number;
  description: string | null;
  imageUrl: string | null;
  stock: number;
  categoryId: number | null;
  categoryName: string | null;
  createdAt: string;
  updatedAt: string;
}

/** Body gửi lên khi ADMIN thêm/sửa sản phẩm (ProductRequest). */
export interface ProductRequest {
  name: string;
  price: number;
  description?: string;
  imageUrl?: string;
  stock: number;
  categoryId: number;
}

export interface Category {
  id: number;
  name: string;
  description: string | null;
  productCount: number;
}

export interface CategoryRequest {
  name: string;
  description?: string;
}

/** Cấu trúc phân trang cố định của product-service. */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/** Tham số tìm kiếm/lọc cho GET /api/products. */
export interface ProductQuery {
  keyword?: string;
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  inStock?: boolean;
  page?: number;
  size?: number;
  sort?: string; // vd: "price,asc"
}
