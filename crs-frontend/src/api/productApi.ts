import axiosClient from './axiosClient';

import type {
  PageResponse,
  Product,
  ProductQuery,
  ProductRequest
} from '../types/product';

/**
 * Tất cả đều gọi qua Gateway (http://localhost:8080):
 *   /api/products/**  ->  product-service :8082 /products/**
 */

/** Bỏ các tham số rỗng để URL gọn và backend không nhận chuỗi "" */
function cleanParams(query: ProductQuery) {
  return Object.fromEntries(
    Object.entries(query).filter(
      ([, value]) => value !== undefined && value !== null && value !== ''
    )
  );
}

export const getProducts = (query: ProductQuery = {}) =>
  axiosClient.get<PageResponse<Product>>('/api/products', {
    params: cleanParams(query)
  });

export const getProductById = (id: number) =>
  axiosClient.get<Product>(`/api/products/${id}`);

export const createProduct = (payload: ProductRequest) =>
  axiosClient.post<Product>('/api/products', payload);

export const updateProduct = (id: number, payload: ProductRequest) =>
  axiosClient.put<Product>(`/api/products/${id}`, payload);

export const deleteProduct = (id: number) =>
  axiosClient.delete<void>(`/api/products/${id}`);

/** Upload ảnh: multipart/form-data, field "file" */
export const uploadProductImage = (id: number, file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return axiosClient.post<Product>(`/api/products/${id}/image`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};
