import axiosClient from './axiosClient';
import type { Cart } from '../types/cart';

export const getCart = () => {
  return axiosClient.get<Cart>('/api/cart');
};

export const addCartItem = (
  productId: number,
  quantity: number
) => {
  return axiosClient.post<Cart>(
    '/api/cart/items',
    null,
    {
      params: {
        productId,
        quantity,
      },
    }
  );
};

export const updateCartItem = (
  id: number,
  quantity: number
) => {
  return axiosClient.put<Cart>(
    `/api/cart/items/${id}`,
    null,
    {
      params: {
        quantity,
      },
    }
  );
};

export const removeCartItem = (id: number) => {
  return axiosClient.delete(
    `/api/cart/items/${id}`
  );
};

export const clearCart = () => {
  return axiosClient.delete('/api/cart');
};