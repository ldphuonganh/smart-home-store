import { useState, type FormEvent } from 'react';

import type { Category, Product, ProductRequest } from '../../types/product';

export interface ProductFormSubmit {
  payload: ProductRequest;
  imageFile: File | null;
}

interface ProductFormProps {
  categories: Category[];
  initial?: Product | null; // có initial = chế độ Sửa, không có = Thêm
  serverErrors?: Record<string, string>;
  submitting: boolean;
  onSubmit: (data: ProductFormSubmit) => void;
  onCancel: () => void;
}

interface FormState {
  name: string;
  price: string;
  stock: string;
  categoryId: string;
  description: string;
  imageUrl: string;
}

function toFormState(product?: Product | null): FormState {
  return {
    name: product?.name ?? '',
    price: product ? String(product.price) : '',
    stock: product ? String(product.stock) : '0',
    categoryId: product?.categoryId ? String(product.categoryId) : '',
    description: product?.description ?? '',
    imageUrl: product?.imageUrl ?? ''
  };
}

/**
 * Form dùng chung cho Thêm và Sửa (Controlled Component - Buổi 7).
 * Validate phía client trước khi gửi; lỗi từ server hiển thị dưới từng ô.
 */
export default function ProductForm({
  categories,
  initial,
  serverErrors = {},
  submitting,
  onSubmit,
  onCancel
}: ProductFormProps) {
  const [form, setForm] = useState<FormState>(() => toFormState(initial));
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [clientErrors, setClientErrors] = useState<Record<string, string>>({});

  const errors = { ...serverErrors, ...clientErrors };

  const update = (key: keyof FormState, value: string) => {
    setForm((f) => ({ ...f, [key]: value }));
    setClientErrors((e) => {
      const next = { ...e };
      delete next[key];
      return next;
    });
  };

  const validate = (): Record<string, string> => {
    const e: Record<string, string> = {};
    if (!form.name.trim()) e.name = 'Tên sản phẩm không được để trống';
    else if (form.name.trim().length > 150) e.name = 'Tên sản phẩm tối đa 150 ký tự';
    if (form.price === '' || !Number.isInteger(Number(form.price)) || Number(form.price) < 0)
      e.price = 'Giá phải là số nguyên (VNĐ) lớn hơn hoặc bằng 0';
    if (form.stock === '' || !Number.isInteger(Number(form.stock)) || Number(form.stock) < 0)
      e.stock = 'Tồn kho phải là số nguyên lớn hơn hoặc bằng 0';
    if (!form.categoryId) e.categoryId = 'Vui lòng chọn danh mục';
    if (imageFile && !imageFile.type.startsWith('image/')) e.image = 'Chỉ chấp nhận file ảnh';
    if (imageFile && imageFile.size > 5 * 1024 * 1024) e.image = 'Ảnh tối đa 5MB';
    return e;
  };

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    const e = validate();
    setClientErrors(e);
    if (Object.keys(e).length > 0) {
      return; // Không gọi API khi dữ liệu rõ ràng sai
    }
    onSubmit({
      payload: {
        name: form.name.trim(),
        price: Number(form.price),
        stock: Number(form.stock),
        categoryId: Number(form.categoryId),
        description: form.description.trim() || undefined,
        imageUrl: form.imageUrl.trim() || undefined
      },
      imageFile
    });
  };

  const fieldError = (key: string) =>
    errors[key] ? <span className="sp-field-error">{errors[key]}</span> : null;

  return (
    <form className="sp-form" onSubmit={handleSubmit} noValidate>
      <h3 className="sp-full" style={{ margin: 0 }}>
        {initial ? `Sửa sản phẩm #${initial.id}` : 'Thêm sản phẩm mới'}
      </h3>

      <label className="sp-full">
        Tên sản phẩm *
        <input className="sp-input" value={form.name} onChange={(e) => update('name', e.target.value)} />
        {fieldError('name')}
      </label>

      <label>
        Giá (VNĐ) *
        <input
          className="sp-input"
          type="number"
          min={0}
          step={1000}
          value={form.price}
          onChange={(e) => update('price', e.target.value)}
        />
        {fieldError('price')}
      </label>

      <label>
        Tồn kho *
        <input
          className="sp-input"
          type="number"
          min={0}
          step={1}
          value={form.stock}
          onChange={(e) => update('stock', e.target.value)}
        />
        {fieldError('stock')}
      </label>

      <label>
        Danh mục *
        <select
          className="sp-select"
          value={form.categoryId}
          onChange={(e) => update('categoryId', e.target.value)}
        >
          <option value="">-- Chọn danh mục --</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        {fieldError('categoryId')}
      </label>

      <label>
        Ảnh sản phẩm (tải lên)
        <input
          className="sp-input"
          type="file"
          accept="image/png,image/jpeg,image/webp,image/gif"
          onChange={(e) => {
            setImageFile(e.target.files?.[0] ?? null);
            setClientErrors((prev) => {
              const next = { ...prev };
              delete next.image;
              return next;
            });
          }}
        />
        {fieldError('image')}
      </label>

      <label className="sp-full">
        Hoặc link ảnh
        <input
          className="sp-input"
          placeholder="https://..."
          value={form.imageUrl}
          onChange={(e) => update('imageUrl', e.target.value)}
        />
        {fieldError('imageUrl')}
      </label>

      <label className="sp-full">
        Mô tả
        <textarea
          className="sp-textarea"
          value={form.description}
          onChange={(e) => update('description', e.target.value)}
        />
        {fieldError('description')}
      </label>

      <div className="sp-full sp-row">
        <button type="submit" className="sp-btn sp-btn-primary" disabled={submitting}>
          {submitting ? 'Đang lưu...' : initial ? 'Lưu thay đổi' : 'Thêm sản phẩm'}
        </button>
        <button type="button" className="sp-btn" onClick={onCancel} disabled={submitting}>
          Huỷ
        </button>
      </div>
    </form>
  );
}
