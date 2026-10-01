import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import { addCartItem } from '../api/cartApi';
import { getWishlist, removeWishlistItem } from '../api/wishlistApi';
import ItemThumb from '../components/cart/ItemThumb';
import type { Wishlist, WishlistItem } from '../types/wishlist';
import { formatPrice, getErrorMessage } from '../utils/format';
import './cart.css';

/** Trang sản phẩm yêu thích (/wishlist) - chỉ CUSTOMER. */
export default function WishlistPage() {
  const [wishlist, setWishlist] = useState<Wishlist | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    getWishlist()
      .then(({ data }) => setWishlist(data))
      .catch((err) => setError(getErrorMessage(err, 'Không tải được danh sách yêu thích')))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const remove = async (item: WishlistItem) => {
    try {
      const { data } = await removeWishlistItem(item.id);
      setWishlist(data);
    } catch (err) {
      setMessage({ type: 'error', text: getErrorMessage(err, 'Xoá thất bại') });
    }
  };

  const moveToCart = async (item: WishlistItem) => {
    try {
      await addCartItem(item.productId, 1);
      setMessage({ type: 'success', text: `Đã thêm "${item.productName}" vào giỏ hàng` });
    } catch (err) {
      setMessage({ type: 'error', text: getErrorMessage(err, 'Thêm vào giỏ thất bại') });
    }
  };

  if (loading && !wishlist) {
    return <div className="ct-page"><div className="ct-state">Đang tải...</div></div>;
  }
  if (error) {
    return (
      <div className="ct-page">
        <div className="ct-state ct-error">
          <p>{error}</p>
          <button className="ct-btn" onClick={load}>Thử lại</button>
        </div>
      </div>
    );
  }

  return (
    <div className="ct-page">
      <h1>Sản phẩm yêu thích</h1>
      {message && <p className={message.type === 'error' ? 'ct-error' : ''}>{message.text}</p>}
      {!wishlist || wishlist.items.length === 0 ? (
        <div className="ct-state">
          <p>Chưa có sản phẩm yêu thích.</p>
          <Link to="/products">Khám phá sản phẩm</Link>
        </div>
      ) : (
        wishlist.items.map((item) => (
          <div
            key={item.id}
            className={`ct-row ${item.available ? '' : 'ct-unavailable'}`}
            style={{ gridTemplateColumns: '72px 1fr auto auto' }}
          >
            <ItemThumb imageUrl={item.imageUrl} alt={item.productName} />
            <div>
              <Link className="ct-name" to={`/products/${item.productId}`}>{item.productName}</Link>
              <div className="ct-price">{item.price != null ? formatPrice(item.price) : ''}</div>
            </div>
            <button
              className="ct-btn"
              disabled={!item.available || !item.stock}
              onClick={() => moveToCart(item)}
            >
              {item.available && !item.stock ? 'Hết hàng' : 'Thêm vào giỏ'}
            </button>
            <button className="ct-btn ct-btn-danger" onClick={() => remove(item)}>Bỏ thích</button>
          </div>
        ))
      )}
    </div>
  );
}
