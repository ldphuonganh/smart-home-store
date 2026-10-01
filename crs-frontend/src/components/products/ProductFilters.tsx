import type { Category } from '../../types/product';

export interface FilterValues {
  keyword: string;
  categoryId: string;
  minPrice: string;
  maxPrice: string;
  inStock: boolean;
}

export const EMPTY_FILTERS: FilterValues = {
  keyword: '',
  categoryId: '',
  minPrice: '',
  maxPrice: '',
  inStock: false
};

interface ProductFiltersProps {
  values: FilterValues;
  categories: Category[];
  onChange: (values: FilterValues) => void;
}

/** Bộ lọc dạng Controlled Component: giá trị do trang cha quản lý. */
export default function ProductFilters({ values, categories, onChange }: ProductFiltersProps) {
  const update = <K extends keyof FilterValues>(key: K, value: FilterValues[K]) =>
    onChange({ ...values, [key]: value });

  const priceError =
    values.minPrice !== '' &&
    values.maxPrice !== '' &&
    Number(values.minPrice) > Number(values.maxPrice);

  return (
    <aside className="sp-filters">
      <label>
        Tìm kiếm
        <input
          className="sp-input"
          type="search"
          placeholder="Tên sản phẩm..."
          value={values.keyword}
          onChange={(e) => update('keyword', e.target.value)}
        />
      </label>

      <label>
        Danh mục
        <select
          className="sp-select"
          value={values.categoryId}
          onChange={(e) => update('categoryId', e.target.value)}
        >
          <option value="">Tất cả danh mục</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </label>

      <label>
        Giá từ (đ)
        <input
          className="sp-input"
          type="number"
          min={0}
          step={10000}
          value={values.minPrice}
          onChange={(e) => update('minPrice', e.target.value)}
        />
      </label>

      <label>
        Đến (đ)
        <input
          className="sp-input"
          type="number"
          min={0}
          step={10000}
          value={values.maxPrice}
          onChange={(e) => update('maxPrice', e.target.value)}
        />
      </label>
      {priceError && <span className="sp-field-error">Giá từ phải nhỏ hơn hoặc bằng giá đến</span>}

      <label className="sp-checkbox">
        <input
          type="checkbox"
          checked={values.inStock}
          onChange={(e) => update('inStock', e.target.checked)}
        />
        Chỉ hiện sản phẩm còn hàng
      </label>

      <button className="sp-btn" onClick={() => onChange(EMPTY_FILTERS)}>
        Xoá bộ lọc
      </button>
    </aside>
  );
}
