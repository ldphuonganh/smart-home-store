import axiosClient from './axiosClient';
import type { Wishlist } from '../types/wishlist';

/** /api/wishlist/** -> cart-service :8084 /wishlist/** */

export const getWishlist = () => axiosClient.get<Wishlist>('/api/wishlist');

export const addWishlistItem = (productId: number) =>
  axiosClient.post<Wishlist>('/api/wishlist/items', { productId });

export const removeWishlistItem = (id: number) =>
  axiosClient.delete<Wishlist>(`/api/wishlist/items/${id}`);

export const clearWishlist = () => axiosClient.delete<void>('/api/wishlist');
