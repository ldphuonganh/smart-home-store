export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'SHIPPING'
  | 'COMPLETED'
  | 'CANCELLED';

export interface OrderItemRequest {
  productId: number;
  quantity: number;

  // Dùng khi product-service chưa bật validation.
  // Giúp TV4 có thể test độc lập.
  productName?: string;
  price?: number;
}

export interface OrderRequest {
  shippingAddress: string;
  phone: string;
  paymentMethod: string;
  items: OrderItemRequest[];
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  price: number;
  quantity: number;
  subtotal: number;
}

export interface Order {
  id: number;
  customerId: number;
  shippingAddress: string;
  phone: string;
  paymentMethod: string;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
  items: OrderItem[];
}

export interface UpdateOrderStatusRequest {
  status: OrderStatus;
}