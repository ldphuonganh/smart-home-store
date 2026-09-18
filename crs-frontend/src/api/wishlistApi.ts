import axiosClient from './axiosClient';
import type { Wishlist } from '../types/wishlist';

export const getWishlist = () => {
  return axiosClient.get<Wishlist>('/api/wishlist');
};

export const addWishlistItem = (
  productId: number
) => {
  return axiosClient.post<Wishlist>(
    '/api/wishlist/items',
    null,
    {
      params: {
        productId,
      },
    }
  );
};

export const removeWishlistItem = (
  id: number
) => {
  return axiosClient.delete(
    `/api/wishlist/items/${id}`
  );
};

export const clearWishlist = () => {
  return axiosClient.delete('/api/wishlist');
};