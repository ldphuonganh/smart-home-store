import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import { clearCart, getCart, removeCartItem, updateCartItem } from '../api/cartApi';
import ItemThumb from '../components/cart/ItemThumb';
import type { Cart, CartItem } from '../types/cart';
import { formatPrice, getErrorMessage } from '../utils/format';
import './cart.css';

/** Trang giỏ hàng (/cart) - chỉ CUSTOMER. */
export default function CartPage() {
  const navigate = useNavigate();
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    getCart()
      .then(({ data }) => setCart(data))
      .catch((err) => setError(getErrorMessage(err, 'Không tải được giỏ hàng')))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  // Mỗi thao tác trả về giỏ mới -> cập nhật ngay, không cần gọi lại GET
  const runAction = async (itemId: number | null, action: () => Promise<{ data: Cart }>) => {
    setActionError(null);
    setBusyId(itemId);
    try {
      const { data } = await action();
      setCart(data);
    } catch (err) {
      setActionError(getErrorMessage(err, 'Thao tác thất bại'));
    } finally {
      setBusyId(null);
    }
  };

  const changeQuantity = (item: CartItem, quantity: number) => {
    if (quantity < 1 || quantity === item.quantity) return;
    runAction(item.id, () => updateCartItem(item.id, quantity));
  };

  const removeItem = (item: CartItem) => {
    if (!window.confirm(`Xoá "${item.productName}" khỏi giỏ hàng?`)) return;
    runAction(item.id, () => removeCartItem(item.id));
  };

  const handleClear = async () => {
    if (!window.confirm('Xoá toàn bộ giỏ hàng?')) return;
    try {
      await clearCart();
      load();
    } catch (err) {
      setActionError(getErrorMessage(err, 'Xoá giỏ hàng thất bại'));
    }
  };

  const availableItems = cart?.items.filter((i) => i.available) ?? [];

  /** Sang trang Checkout của TV4 với các sản phẩm còn bán. */
  const handleCheckout = () => {
    navigate('/checkout', {
      state: {
        fromCart: true,
        items: availableItems.map((i) => ({
          productId: i.productId,
          productName: i.productName,
          price: i.price,
          quantity: i.quantity
        }))
      }
    });
  };

  if (loading && !cart) {
    return <div className="ct-page"><div className="ct-state">Đang tải giỏ hàng...</div></div>;
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

  if (!cart || cart.items.length === 0) {
    return (
      <div className="ct-page">
        <h1>Giỏ hàng</h1>
        <div className="ct-state">
          <p>Giỏ hàng đang trống.</p>
          <Link to="/products">Tiếp tục mua sắm</Link>
        </div>
      </div>
    );
  }

  const overStock = availableItems.some((i) => i.quantity > i.stock);

  return (
    <div className="ct-page">
      <h1>Giỏ hàng ({cart.totalQuantity})</h1>
      {actionError && <p className="ct-error">{actionError}</p>}

      <div className="ct-layout">
        <section>
          {cart.items.map((item) => (
            <div key={item.id} className={`ct-row ${item.available ? '' : 'ct-unavailable'}`}>
              <ItemThumb imageUrl={item.imageUrl} alt={item.productName} />
              <div>
                <Link className="ct-name" to={`/products/${item.productId}`}>{item.productName}</Link>
                <div className="ct-muted">
                  {item.available ? `${formatPrice(item.price)} · còn ${item.stock}` : 'Sản phẩm không còn bán'}
                </div>
                {item.available && item.quantity > item.stock && (
                  <div className="ct-error">Vượt quá tồn kho, vui lòng giảm số lượng</div>
                )}
              </div>
              <div className="ct-qty">
                <button
                  className="ct-btn"
                  disabled={busyId === item.id || item.quantity <= 1 || !item.available}
                  onClick={() => changeQuantity(item, item.quantity - 1)}
                >
                  −
                </button>
                <span style={{ minWidth: 28, textAlign: 'center' }}>{item.quantity}</span>
                <button
                  className="ct-btn"
                  disabled={busyId === item.id || !item.available || item.quantity >= item.stock}
                  onClick={() => changeQuantity(item, item.quantity + 1)}
                >
                  +
                </button>
              </div>
              <span className="ct-price">{item.available ? formatPrice(item.subtotal) : '—'}</span>
              <button
                className="ct-btn ct-btn-danger"
                disabled={busyId === item.id}
                onClick={() => removeItem(item)}
              >
                Xoá
              </button>
            </div>
          ))}
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 12 }}>
            <Link to="/products">← Tiếp tục mua sắm</Link>
            <button className="ct-btn ct-btn-danger" onClick={handleClear}>Xoá toàn bộ</button>
          </div>
        </section>

        <aside className="ct-summary">
          <strong>Tổng cộng</strong>
          <span className="ct-price" style={{ fontSize: 24 }}>{formatPrice(cart.totalAmount)}</span>
          <span className="ct-muted">Giá được cập nhật theo giá hiện tại của sản phẩm.</span>
          <button
            className="ct-btn ct-btn-primary"
            disabled={availableItems.length === 0 || overStock}
            onClick={handleCheckout}
          >
            Đặt hàng
          </button>
        </aside>
      </div>
    </div>
  );
}
