import axiosClient from './axiosClient';
import type { Payment } from '../types/payment';

/** /api/payments/** -> payment-service :8085 /payments/** */

/** Tạo (hoặc lấy lại) thanh toán cho đơn. Số tiền do server lấy từ order-service. */
export const createPayment = (orderId: number) =>
  axiosClient.post<Payment>('/api/payments', { orderId });

/** Giả lập ngân hàng xác nhận đã nhận chuyển khoản. */
export const confirmPayment = (paymentId: number) =>
  axiosClient.post<Payment>(`/api/payments/${paymentId}/confirm`);

export const cancelPayment = (paymentId: number) =>
  axiosClient.post<Payment>(`/api/payments/${paymentId}/cancel`);

export const getPayment = (paymentId: number) =>
  axiosClient.get<Payment>(`/api/payments/${paymentId}`);

export const getMyPayments = () => axiosClient.get<Payment[]>('/api/payments/my');

/** Chỉ ADMIN */
export const getAllPayments = () => axiosClient.get<Payment[]>('/api/payments');
