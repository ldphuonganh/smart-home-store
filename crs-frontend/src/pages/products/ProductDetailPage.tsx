import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';

import { getProductById } from '../../api/productApi';
import ProductImage from '../../components/products/ProductImage';
import StockBadge from '../../components/products/StockBadge';
import { useAuth } from '../../context/AuthContext';
import type { Product } from '../../types/product';
import { formatPrice, getErrorMessage } from '../../utils/format';
import './products.css';

export default function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();

  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    const productId = Number(id);
    if (!Number.isInteger(productId) || productId <= 0) {
      setError('Mã sản phẩm không hợp lệ');
      setLoading(false);
      return;
    }
    let cancelled = false;
    setLoading(true);
    setError(null);
    getProductById(productId)
      .then((res) => {
        if (!cancelled) {
          setProduct(res.data);
          setQuantity(1);
        }
      })
      .catch((err) => {
        if (!cancelled) setError(getErrorMessage(err, 'Không tải được sản phẩm'));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  if (loading) {
    return <div className="sp-page"><div className="sp-state">Đang tải sản phẩm...</div></div>;
  }

  if (error || !product) {
    return (
      <div className="sp-page">
        <div className="sp-state sp-error">
          <p>{error ?? 'Không tìm thấy sản phẩm'}</p>
          <Link to="/products">← Quay lại danh sách</Link>
        </div>
      </div>
    );
  }

  const outOfStock = product.stock <= 0;

  /**
   * "Mua ngay": chuyển sang trang Checkout của TV4 kèm sản phẩm đã chọn
   * (CheckoutPage đọc location.state.items). Giá cuối cùng vẫn do order-service
   * lấy lại từ product-service, không tin giá gửi từ client.
   */
  const handleBuyNow = () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    navigate('/checkout', {
      state: {
        items: [
          {
            productId: product.id,
            productName: product.name,
            price: product.price,
            quantity
          }
        ]
      }
    });
  };

  return (
    <div className="sp-page">
      <p className="sp-muted">
        <Link to="/products">Sản phẩm</Link>
        {product.categoryName && <> / {product.categoryName}</>}
      </p>

      <div className="sp-detail">
        <div className="sp-detail-image">
          <ProductImage imageUrl={product.imageUrl} alt={product.name} />
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <h1>{product.name}</h1>
          <span className="sp-price sp-detail-price">{formatPrice(product.price)}</span>
          <StockBadge stock={product.stock} />
          {!outOfStock && <span className="sp-muted">Còn {product.stock} sản phẩm trong kho</span>}

          {user?.role !== 'ADMIN' && (
            <div className="sp-row">
              <label className="sp-row">
                Số lượng
                <input
                  className="sp-input"
                  style={{ width: 90 }}
                  type="number"
                  min={1}
                  max={product.stock}
                  value={quantity}
                  disabled={outOfStock}
                  onChange={(e) => {
                    const value = Math.floor(Number(e.target.value));
                    setQuantity(Math.min(Math.max(1, value || 1), Math.max(1, product.stock)));
                  }}
                />
              </label>
              <button className="sp-btn sp-btn-primary" disabled={outOfStock} onClick={handleBuyNow}>
                {outOfStock ? 'Tạm hết hàng' : 'Mua ngay'}
              </button>
            </div>
          )}

          {user?.role === 'ADMIN' && (
            <Link className="sp-btn" to="/admin/products" style={{ width: 'fit-content' }}>
              Quản lý sản phẩm
            </Link>
          )}

          <h3 style={{ marginBottom: 0 }}>Mô tả sản phẩm</h3>
          <p className="sp-description">{product.description || 'Chưa có mô tả.'}</p>
        </div>
      </div>
    </div>
  );
}
