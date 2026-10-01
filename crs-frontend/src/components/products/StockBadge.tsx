export default function StockBadge({ stock }: { stock: number }) {
  if (stock <= 0) {
    return <span className="sp-badge sp-badge-out">Hết hàng</span>;
  }
  if (stock <= 5) {
    return <span className="sp-badge sp-badge-low">Chỉ còn {stock}</span>;
  }
  return <span className="sp-badge">Còn hàng</span>;
}
