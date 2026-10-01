import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import { getAllPayments } from '../api/paymentApi';
import type { Payment, PaymentStatus } from '../types/payment';
import { formatPrice, getErrorMessage } from '../utils/format';
import './payment.css';

const STATUS_LABEL: Record<PaymentStatus, string> = {
  PENDING: 'Chờ thanh toán',
  PAID: 'Đã thanh toán',
  FAILED: 'Thất bại',
  CANCELLED: 'Đã huỷ'
};

/** Admin xem toàn bộ giao dịch thanh toán (/admin/payments). */
export default function AdminPaymentsPage() {
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [status, setStatus] = useState<'' | PaymentStatus>('');

  useEffect(() => {
    getAllPayments()
      .then(({ data }) => setPayments(data))
      .catch((err) => setError(getErrorMessage(err, 'Không tải được danh sách thanh toán')))
      .finally(() => setLoading(false));
  }, []);

  const shown = status ? payments.filter((p) => p.status === status) : payments;
  const paidTotal = payments.filter((p) => p.status === 'PAID').reduce((sum, p) => sum + Number(p.amount), 0);

  return (
    <div className="pm-page">
      <h1>Quản lý thanh toán</h1>
      <p className="pm-muted">Tổng đã thu (chuyển khoản): <strong>{formatPrice(paidTotal)}</strong></p>
      <select value={status} onChange={(e) => setStatus(e.target.value as '' | PaymentStatus)} style={{ marginBottom: 12 }}>
        <option value="">Tất cả trạng thái</option>
        {Object.entries(STATUS_LABEL).map(([value, label]) => (
          <option key={value} value={value}>{label}</option>
        ))}
      </select>
      {error && <p className="pm-error">{error}</p>}
      {loading ? (
        <p>Đang tải...</p>
      ) : shown.length === 0 ? (
        <p className="pm-muted">Chưa có giao dịch nào.</p>
      ) : (
        <table className="pm-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Đơn hàng</th>
              <th>Khách hàng</th>
              <th>Số tiền</th>
              <th>Phương thức</th>
              <th>Trạng thái</th>
              <th>Mã GD</th>
              <th>Thời gian</th>
            </tr>
          </thead>
          <tbody>
            {shown.map((p) => (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td><Link to={`/orders/${p.orderId}`}>#{p.orderId}</Link></td>
                <td>#{p.userId}</td>
                <td>{formatPrice(p.amount)}</td>
                <td>{p.paymentMethod === 'COD' ? 'COD' : 'Chuyển khoản'}</td>
                <td><span className="pm-badge">{STATUS_LABEL[p.status]}</span></td>
                <td>{p.transactionId ?? '—'}</td>
                <td>{new Date(p.paidAt ?? p.createdAt).toLocaleString('vi-VN')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
