export interface WishlistItem {
  id: number;
  productId: number;
  productName: string;
  price: number | null;
  imageUrl: string | null;
  stock: number | null;
  available: boolean;
}

export interface Wishlist {
  id: number;
  userId: number;
  items: WishlistItem[];
  createdAt: string;
  updatedAt: string;
}
