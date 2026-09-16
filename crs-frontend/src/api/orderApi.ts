import axiosClient from './axiosClient';

import type {
  Order,
  OrderRequest,
  UpdateOrderStatusRequest
} from '../types/order';

/**
 * Tạo đơn hàng
 */
export const createOrder = (
  payload: OrderRequest
) => {
  return axiosClient.post<Order>(
    '/api/orders',
    payload
  );
};

/**
 * Lấy danh sách đơn hàng của Customer hiện tại
 *
 * Backend hiện tại của TV4 sử dụng:
 * GET /orders/my
 *
 * Gateway:
 * GET /api/orders/my
 */
export const getMyOrders = () => {
  return axiosClient.get<Order[]>(
    '/api/orders/my'
  );
};

/**
 * Lấy tất cả đơn hàng.
 *
 * Chỉ ADMIN được phép gọi API này.
 */
export const getAllOrders = () => {
  return axiosClient.get<Order[]>(
    '/api/orders'
  );
};

/**
 * Lấy chi tiết một đơn hàng.
 */
export const getOrderById = (
  id: number
) => {
  return axiosClient.get<Order>(
    `/api/orders/${id}`
  );
};

/**
 * Customer hủy đơn hàng.
 */
export const cancelOrder = (
  id: number
) => {
  return axiosClient.put<Order>(
    `/api/orders/${id}/cancel`
  );
};

/**
 * ADMIN cập nhật trạng thái đơn hàng.
 */
export const updateOrderStatus = (
  id: number,
  payload: UpdateOrderStatusRequest
) => {
  return axiosClient.put<Order>(
    `/api/orders/${id}/status`,
    payload
  );
};