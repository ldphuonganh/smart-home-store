import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';

import { createCategory, deleteCategory, updateCategory } from '../../api/categoryApi';
import Toast from '../../components/Toast';
import { useCategories } from '../../hooks/useCategories';
import { useToast } from '../../hooks/useToast';
import type { Category } from '../../types/product';
import { getErrorMessage } from '../../utils/format';
import './products.css';

/** Trang Admin quản lý danh mục (/admin/categories) - chỉ ADMIN. */
export default function AdminCategoriesPage() {
  const { categories, loading, error, refetch } = useCategories();
  const { toast, showToast, clearToast } = useToast();

  const [editing, setEditing] = useState<Category | null>(null);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [nameError, setNameError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const resetForm = () => {
    setEditing(null);
    setName('');
    setDescription('');
    setNameError(null);
  };

  const startEdit = (category: Category) => {
    setEditing(category);
    setName(category.name);
    setDescription(category.description ?? '');
    setNameError(null);
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!name.trim()) {
      setNameError('Tên danh mục không được để trống');
      return;
    }
    setSubmitting(true);
    try {
      const payload = { name: name.trim(), description: description.trim() || undefined };
      if (editing) {
        await updateCategory(editing.id, payload);
        showToast('Đã cập nhật danh mục', 'success');
      } else {
        await createCategory(payload);
        showToast('Đã thêm danh mục', 'success');
      }
      resetForm();
      refetch();
    } catch (err) {
      showToast(getErrorMessage(err, 'Lưu danh mục thất bại'), 'error');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (category: Category) => {
    if (!window.confirm(`Xoá danh mục "${category.name}"?`)) {
      return;
    }
    try {
      await deleteCategory(category.id);
      showToast('Đã xoá danh mục', 'success');
      if (editing?.id === category.id) resetForm();
      refetch();
    } catch (err) {
      // Danh mục còn sản phẩm -> backend trả 409 kèm message rõ ràng
      showToast(getErrorMessage(err, 'Xoá danh mục thất bại'), 'error');
    }
  };

  return (
    <div className="sp-page">
      <div className="sp-toolbar">
        <h1 style={{ margin: 0 }}>Quản lý danh mục</h1>
        <Link className="sp-btn" to="/admin/products">
          ← Quản lý sản phẩm
        </Link>
      </div>

      <form className="sp-form" onSubmit={handleSubmit} noValidate>
        <h3 className="sp-full" style={{ margin: 0 }}>
          {editing ? `Sửa danh mục #${editing.id}` : 'Thêm danh mục'}
        </h3>
        <label>
          Tên danh mục *
          <input
            className="sp-input"
            value={name}
            onChange={(e) => {
              setName(e.target.value);
              setNameError(null);
            }}
          />
          {nameError && <span className="sp-field-error">{nameError}</span>}
        </label>
        <label>
          Mô tả
          <input className="sp-input" value={description} onChange={(e) => setDescription(e.target.value)} />
        </label>
        <div className="sp-full sp-row">
          <button className="sp-btn sp-btn-primary" type="submit" disabled={submitting}>
            {submitting ? 'Đang lưu...' : editing ? 'Lưu thay đổi' : 'Thêm danh mục'}
          </button>
          {editing && (
            <button className="sp-btn" type="button" onClick={resetForm}>
              Huỷ
            </button>
          )}
        </div>
      </form>

      {loading && <div className="sp-state">Đang tải...</div>}
      {error && (
        <div className="sp-state sp-error">
          <p>{error}</p>
          <button className="sp-btn" onClick={refetch}>
            Thử lại
          </button>
        </div>
      )}
      {!loading && !error && categories.length === 0 && (
        <div className="sp-state">Chưa có danh mục nào.</div>
      )}
      {!loading && !error && categories.length > 0 && (
        <div className="sp-table-wrap">
          <table className="sp-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Tên</th>
                <th>Mô tả</th>
                <th>Số sản phẩm</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {categories.map((c) => (
                <tr key={c.id}>
                  <td>{c.id}</td>
                  <td>{c.name}</td>
                  <td>{c.description ?? '—'}</td>
                  <td>{c.productCount}</td>
                  <td>
                    <div className="sp-row">
                      <button className="sp-btn sp-btn-sm" onClick={() => startEdit(c)}>
                        Sửa
                      </button>
                      <button
                        className="sp-btn sp-btn-sm sp-btn-danger"
                        onClick={() => handleDelete(c)}
                        title={c.productCount > 0 ? 'Danh mục còn sản phẩm, không thể xoá' : undefined}
                      >
                        Xoá
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {toast && <Toast message={toast.message} type={toast.type} onClose={clearToast} />}
    </div>
  );
}
