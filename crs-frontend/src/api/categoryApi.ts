import axiosClient from './axiosClient';

import type { Category, CategoryRequest } from '../types/product';

/** /api/categories/**  ->  product-service :8082 /categories/** */

export const getCategories = () =>
  axiosClient.get<Category[]>('/api/categories');

export const createCategory = (payload: CategoryRequest) =>
  axiosClient.post<Category>('/api/categories', payload);

export const updateCategory = (id: number, payload: CategoryRequest) =>
  axiosClient.put<Category>(`/api/categories/${id}`, payload);

export const deleteCategory = (id: number) =>
  axiosClient.delete<void>(`/api/categories/${id}`);
