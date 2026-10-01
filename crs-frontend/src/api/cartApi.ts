import axiosClient from './axiosClient';
import type { Cart } from '../types/cart';

/** /api/cart/** -> cart-service :8084 /cart/** (body JSON theo API Contract mục 28) */

export const getCart = () => axiosClient.get<Cart>('/api/cart');

export const addCartItem = (productId: number, quantity: number) =>
  axiosClient.post<Cart>('/api/cart/items', { productId, quantity });

export const updateCartItem = (id: number, quantity: number) =>
  axiosClient.put<Cart>(`/api/cart/items/${id}`, { quantity });

export const removeCartItem = (id: number) =>
  axiosClient.delete<Cart>(`/api/cart/items/${id}`);

export const clearCart = () => axiosClient.delete<void>('/api/cart');
