import { Link } from 'react-router-dom';

import type { Product } from '../../types/product';
import { formatPrice } from '../../utils/format';
import ProductImage from './ProductImage';
import StockBadge from './StockBadge';

export default function ProductCard({ product }: { product: Product }) {
  return (
    <Link to={`/products/${product.id}`} className="sp-card">
      <ProductImage imageUrl={product.imageUrl} alt={product.name} />
      <div className="sp-card-body">
        {product.categoryName && <span className="sp-muted">{product.categoryName}</span>}
        <span className="sp-card-name">{product.name}</span>
        <span className="sp-price">{formatPrice(product.price)}</span>
        <StockBadge stock={product.stock} />
      </div>
    </Link>
  );
}
