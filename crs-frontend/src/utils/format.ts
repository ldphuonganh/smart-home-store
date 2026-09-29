import axios from 'axios';

import type { ApiErrorResponse } from '../types/apiError';

const priceFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0
});

export function formatPrice(value: number | null | undefined): string {
  return priceFormatter.format(Number(value ?? 0));
}

/**
 * imageUrl do product-service trả về dạng "/api/products/images/xxx.png"
 * -> ghép với địa chỉ Gateway để thẻ <img> tải được.
 * Link tuyệt đối (http...) thì giữ nguyên.
 */
export function resolveImageUrl(imageUrl: string | null | undefined): string | null {
  if (!imageUrl) {
    return null;
  }
  if (/^https?:\/\//i.test(imageUrl)) {
    return imageUrl;
  }
  const base = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '');
  return `${base}${imageUrl.startsWith('/') ? '' : '/'}${imageUrl}`;
}

/** Lấy message lỗi theo format chung, kể cả khi lỗi đến từ Gateway / mất kết nối. */
export function getErrorMessage(err: unknown, fallback = 'Đã có lỗi xảy ra'): string {
  if (axios.isAxiosError<ApiErrorResponse>(err)) {
    if (!err.response) {
      return 'Không kết nối được tới máy chủ (Gateway hoặc service đang tắt)';
    }
    const data = err.response.data;
    if (data && typeof data === 'object' && typeof data.message === 'string') {
      return data.message;
    }
    if (err.response.status === 401) return 'Bạn cần đăng nhập để thực hiện thao tác này';
    if (err.response.status === 403) return 'Bạn không có quyền thực hiện thao tác này';
    if (err.response.status === 404) return 'Không tìm thấy dữ liệu';
  }
  return fallback;
}

/** Lỗi từng field (khi backend trả 400 kèm errors). */
export function getFieldErrors(err: unknown): Record<string, string> {
  if (axios.isAxiosError<ApiErrorResponse>(err) && err.response?.data?.errors) {
    return err.response.data.errors;
  }
  return {};
}
