// Khớp CartDTO / CartItemDTO của cart-service

export interface CartItem {
  id: number; // id của dòng trong giỏ (dùng cho PUT/DELETE /api/cart/items/{id})
  productId: number;
  productName: string;
  price: number;
  imageUrl: string | null;
  stock: number;
  quantity: number;
  subtotal: number;
  available: boolean; // false = sản phẩm đã bị xoá / không lấy được thông tin
}

export interface Cart {
  id: number;
  userId: number;
  items: CartItem[];
  totalQuantity: number;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
}
