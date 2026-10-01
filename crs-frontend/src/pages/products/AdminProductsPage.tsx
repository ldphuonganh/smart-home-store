import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';

import {
  createProduct,
  deleteProduct,
  updateProduct,
  uploadProductImage
} from '../../api/productApi';
import Pagination from '../../components/Pagination';
import Toast from '../../components/Toast';
import ProductForm, { type ProductFormSubmit } from '../../components/products/ProductForm';
import ProductImage from '../../components/products/ProductImage';
import StockBadge from '../../components/products/StockBadge';
import { useCategories } from '../../hooks/useCategories';
import { useDebounce } from '../../hooks/useDebounce';
import { useProducts } from '../../hooks/useProducts';
import { useToast } from '../../hooks/useToast';
import type { Product } from '../../types/product';
import { formatPrice, getErrorMessage, getFieldErrors } from '../../utils/format';
import './products.css';

type FormMode = { type: 'closed' } | { type: 'create' } | { type: 'edit'; product: Product };

/** Trang Admin quản lý sản phẩm (/admin/products) - chỉ ADMIN. */
export default function AdminProductsPage() {
  const [keyword, setKeyword] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [page, setPage] = useState(0);
  const [formMode, setFormMode] = useState<FormMode>({ type: 'closed' });
  const [submitting, setSubmitting] = useState(false);
  const [serverErrors, setServerErrors] = useState<Record<string, string>>({});

  const { toast, showToast, clearToast } = useToast();
  const { categories, refetch: refetchCategories } = useCategories();
  const debouncedKeyword = useDebounce(keyword);

  const query = useMemo(
    () => ({
      keyword: debouncedKeyword.trim() || undefined,
      categoryId: categoryId ? Number(categoryId) : undefined,
      page,
      size: 10,
      sort: 'id,desc'
    }),
    [debouncedKeyword, categoryId, page]
  );
  const { data, loading, error, refetch } = useProducts(query);

  const openForm = (mode: FormMode) => {
    setServerErrors({});
    setFormMode(mode);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleSubmit = async ({ payload, imageFile }: ProductFormSubmit) => {
    setSubmitting(true);
    setServerErrors({});
    try {
      const saved =
        formMode.type === 'edit'
          ? (await updateProduct(formMode.product.id, payload)).data
          : (await createProduct(payload)).data;

      if (imageFile) {
        try {
          await uploadProductImage(saved.id, imageFile);
        } catch (err) {
          showToast(`Đã lưu sản phẩm nhưng tải ảnh thất bại: ${getErrorMessage(err)}`, 'error');
        }
      }

      showToast(formMode.type === 'edit' ? 'Đã cập nhật sản phẩm' : 'Đã thêm sản phẩm', 'success');
      setFormMode({ type: 'closed' });
      refetch(); // Đồng bộ lại danh sách, không cần F5
      refetchCategories(); // Cập nhật số sản phẩm theo danh mục
    } catch (err) {
      setServerErrors(getFieldErrors(err));
      showToast(getErrorMessage(err, 'Lưu sản phẩm thất bại'), 'error');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (product: Product) => {
    if (!window.confirm(`Xoá sản phẩm "${product.name}"?`)) {
      return;
    }
    try {
      await deleteProduct(product.id);
      showToast('Đã xoá sản phẩm', 'success');
      // Xoá phần tử cuối cùng của trang -> lùi về trang trước
      if (data && data.content.length === 1 && page > 0) {
        setPage(page - 1);
      } else {
        refetch();
      }
      refetchCategories();
    } catch (err) {
      showToast(getErrorMessage(err, 'Xoá sản phẩm thất bại'), 'error');
    }
  };

  return (
    <div className="sp-page">
      <div className="sp-toolbar">
        <h1 style={{ margin: 0 }}>Quản lý sản phẩm</h1>
        <div className="sp-row">
          <Link className="sp-btn" to="/admin/categories">
            Quản lý danh mục
          </Link>
          <button className="sp-btn sp-btn-primary" onClick={() => openForm({ type: 'create' })}>
            + Thêm sản phẩm
          </button>
        </div>
      </div>

      {formMode.type !== 'closed' && (
        <ProductForm
          // key để form reset khi chuyển giữa Thêm / Sửa sản phẩm khác
          key={formMode.type === 'edit' ? formMode.product.id : 'new'}
          categories={categories}
          initial={formMode.type === 'edit' ? formMode.product : null}
          serverErrors={serverErrors}
          submitting={submitting}
          onSubmit={handleSubmit}
          onCancel={() => setFormMode({ type: 'closed' })}
        />
      )}

      <div className="sp-toolbar">
        <input
          className="sp-input"
          style={{ maxWidth: 320 }}
          type="search"
          placeholder="Tìm theo tên..."
          value={keyword}
          onChange={(e) => {
            setKeyword(e.target.value);
            setPage(0);
          }}
        />
        <select
          className="sp-select"
          style={{ maxWidth: 240 }}
          value={categoryId}
          onChange={(e) => {
            setCategoryId(e.target.value);
            setPage(0);
          }}
        >
          <option value="">Tất cả danh mục</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </div>

      {loading && !data && <div className="sp-state">Đang tải...</div>}
      {error && (
        <div className="sp-state sp-error">
          <p>{error}</p>
          <button className="sp-btn" onClick={refetch}>
            Thử lại
          </button>
        </div>
      )}
      {!error && data && data.content.length === 0 && (
        <div className="sp-state">Chưa có sản phẩm nào.</div>
      )}

      {!error && data && data.content.length > 0 && (
        <>
          <div className="sp-table-wrap" style={{ opacity: loading ? 0.5 : 1 }}>
            <table className="sp-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Ảnh</th>
                  <th>Tên</th>
                  <th>Danh mục</th>
                  <th>Giá</th>
                  <th>Tồn kho</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((p) => (
                  <tr key={p.id}>
                    <td>{p.id}</td>
                    <td>
                      <ProductImage imageUrl={p.imageUrl} alt={p.name} className="sp-table-thumb" />
                    </td>
                    <td>
                      <Link to={`/products/${p.id}`}>{p.name}</Link>
                    </td>
                    <td>{p.categoryName ?? '—'}</td>
                    <td className="sp-price">{formatPrice(p.price)}</td>
                    <td>
                      {p.stock} <StockBadge stock={p.stock} />
                    </td>
                    <td>
                      <div className="sp-row">
                        <button
                          className="sp-btn sp-btn-sm"
                          onClick={() => openForm({ type: 'edit', product: p })}
                        >
                          Sửa
                        </button>
                        <button className="sp-btn sp-btn-sm sp-btn-danger" onClick={() => handleDelete(p)}>
                          Xoá
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}

      {toast && <Toast message={toast.message} type={toast.type} onClose={clearToast} />}
    </div>
  );
}
