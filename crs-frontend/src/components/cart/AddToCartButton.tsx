import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { addCartItem } from '../../api/cartApi';
import { addWishlistItem } from '../../api/wishlistApi';
import { useAuth } from '../../context/AuthContext';
import { getErrorMessage } from '../../utils/format';

interface AddToCartButtonProps {
  productId: number;
  quantity?: number;
  disabled?: boolean;
  /** Hiện thêm nút "Yêu thích" */
  showWishlist?: boolean;
  onDone?: (message: string, type: 'success' | 'error') => void;
}

/**
 * Nút "Thêm vào giỏ" (+ "Yêu thích") dùng lại ở trang chi tiết sản phẩm của TV2.
 * Chưa đăng nhập -> chuyển tới /login.
 */
export default function AddToCartButton({
  productId,
  quantity = 1,
  disabled,
  showWishlist = true,
  onDone
}: AddToCartButtonProps) {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const [busy, setBusy] = useState(false);

  if (user?.role === 'ADMIN') {
    return null; // Admin không mua hàng
  }

  const run = async (action: () => Promise<unknown>, success: string) => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: window.location.pathname } });
      return;
    }
    setBusy(true);
    try {
      await action();
      onDone?.(success, 'success');
    } catch (err) {
      onDone?.(getErrorMessage(err, 'Thao tác thất bại'), 'error');
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <button
        className="sp-btn sp-btn-primary"
        disabled={disabled || busy}
        onClick={() => run(() => addCartItem(productId, quantity), 'Đã thêm vào giỏ hàng')}
      >
        Thêm vào giỏ
      </button>
      {showWishlist && (
        <button
          className="sp-btn"
          disabled={busy}
          onClick={() => run(() => addWishlistItem(productId), 'Đã thêm vào danh sách yêu thích')}
        >
          ♡ Yêu thích
        </button>
      )}
    </>
  );
}
