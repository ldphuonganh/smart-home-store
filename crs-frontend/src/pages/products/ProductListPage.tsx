import { useMemo, useState } from 'react';

import Pagination from '../../components/Pagination';
import ProductCard from '../../components/products/ProductCard';
import ProductFilters, {
  EMPTY_FILTERS,
  type FilterValues
} from '../../components/products/ProductFilters';
import { useCategories } from '../../hooks/useCategories';
import { useDebounce } from '../../hooks/useDebounce';
import { useProducts } from '../../hooks/useProducts';
import type { ProductQuery } from '../../types/product';
import './products.css';

const PAGE_SIZE = 12;

const SORT_OPTIONS = [
  { value: 'id,desc', label: 'Mới nhất' },
  { value: 'price,asc', label: 'Giá tăng dần' },
  { value: 'price,desc', label: 'Giá giảm dần' },
  { value: 'name,asc', label: 'Tên A → Z' }
];

/** Trang danh sách sản phẩm - public, khách chưa đăng nhập vẫn xem được. */
export default function ProductListPage() {
  const [filters, setFilters] = useState<FilterValues>(EMPTY_FILTERS);
  const [sort, setSort] = useState('id,desc');
  const [page, setPage] = useState(0);

  const { categories } = useCategories();

  // Ô nhập chữ/số được debounce để không gọi API mỗi lần gõ phím
  const keyword = useDebounce(filters.keyword);
  const minPrice = useDebounce(filters.minPrice);
  const maxPrice = useDebounce(filters.maxPrice);

  const priceInvalid =
    minPrice !== '' && maxPrice !== '' && Number(minPrice) > Number(maxPrice);

  const query: ProductQuery = useMemo(
    () => ({
      keyword: keyword.trim() || undefined,
      categoryId: filters.categoryId ? Number(filters.categoryId) : undefined,
      minPrice: minPrice !== '' && !priceInvalid ? Number(minPrice) : undefined,
      maxPrice: maxPrice !== '' && !priceInvalid ? Number(maxPrice) : undefined,
      inStock: filters.inStock || undefined,
      page,
      size: PAGE_SIZE,
      sort
    }),
    [keyword, filters.categoryId, minPrice, maxPrice, priceInvalid, filters.inStock, page, sort]
  );

  const { data, loading, error, refetch } = useProducts(query);

  // Đổi bộ lọc luôn quay về trang đầu (tránh "trang 3 không có kết quả")
  const handleFiltersChange = (next: FilterValues) => {
    setFilters(next);
    setPage(0);
  };

  const renderContent = () => {
    if (loading && !data) {
      return <div className="sp-state">Đang tải sản phẩm...</div>;
    }
    if (error) {
      return (
        <div className="sp-state sp-error">
          <p>{error}</p>
          <button className="sp-btn" onClick={refetch}>
            Thử lại
          </button>
        </div>
      );
    }
    if (!data || data.content.length === 0) {
      return (
        <div className="sp-state">
          <p>Không tìm thấy sản phẩm phù hợp.</p>
          <button className="sp-btn" onClick={() => handleFiltersChange(EMPTY_FILTERS)}>
            Xoá bộ lọc
          </button>
        </div>
      );
    }
    return (
      <>
        <div className="sp-grid" style={{ opacity: loading ? 0.5 : 1 }}>
          {data.content.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
        <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
      </>
    );
  };

  return (
    <div className="sp-page">
      <h1>Sản phẩm nhà thông minh</h1>
      <div className="sp-layout">
        <ProductFilters values={filters} categories={categories} onChange={handleFiltersChange} />

        <section>
          <div className="sp-toolbar">
            <span className="sp-muted">
              {data ? `${data.totalElements} sản phẩm` : ' '}
            </span>
            <label className="sp-row sp-muted">
              Sắp xếp
              <select
                className="sp-select"
                style={{ width: 'auto' }}
                value={sort}
                onChange={(e) => {
                  setSort(e.target.value);
                  setPage(0);
                }}
              >
                {SORT_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </label>
          </div>
          {renderContent()}
        </section>
      </div>
    </div>
  );
}
