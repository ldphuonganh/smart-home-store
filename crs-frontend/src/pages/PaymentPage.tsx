import { useEffect, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';

import { cancelPayment, confirmPayment, createPayment } from '../api/paymentApi';
import type { Payment } from '../types/payment';
import { formatPrice, getErrorMessage } from '../utils/format';
import './payment.css';

/**
 * Trang thanh toán cho 1 đơn (/payment/:orderId) - CUSTOMER.
 * Chuyển khoản: hiển thị QR (VietQR) + thông tin tài khoản; bấm "Tôi đã chuyển khoản"
 * để GIẢ LẬP ngân hàng xác nhận (đồ án không kết nối ngân hàng thật).
 */
export default function PaymentPage() {
  const { orderId } = useParams();
  const [payment, setPayment] = useState<Payment | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [qrBroken, setQrBroken] = useState(false);
  // StrictMode (dev) gọi useEffect 2 lần -> chặn tạo 2 thanh toán cùng lúc
  const startedFor = useRef<string | null>(null);

  useEffect(() => {
    if (startedFor.current === orderId) return;
    startedFor.current = orderId ?? null;
    const id = Number(orderId);
    if (!Number.isInteger(id) || id <= 0) {
      setError('Mã đơn hàng không hợp lệ');
      setLoading(false);
      return;
    }
    createPayment(id)
      .then(({ data }) => setPayment(data))
      .catch((err) => setError(getErrorMessage(err, 'Không tạo được thanh toán')))
      .finally(() => setLoading(false));
  }, [orderId]);

  const run = async (action: () => Promise<{ data: Payment }>) => {
    setBusy(true);
    setError(null);
    try {
      const { data } = await action();
      setPayment(data);
    } catch (err) {
      setError(getErrorMessage(err, 'Thao tác thất bại'));
    } finally {
      setBusy(false);
    }
  };

  if (loading) {
    return <div className="pm-card">Đang tạo thanh toán...</div>;
  }

  if (!payment) {
    return (
      <div className="pm-card">
        <p className="pm-error">{error}</p>
        <Link to="/my-orders">← Đơn hàng của tôi</Link>
      </div>
    );
  }

  const bank = payment.bankTransfer;
  const qrUrl = bank
    ? `https://img.vietqr.io/image/${bank.bankBin}-${bank.accountNo}-compact2.png` +
      `?amount=${Math.round(payment.amount)}&addInfo=${encodeURIComponent(bank.transferContent)}` +
      `&accountName=${encodeURIComponent(bank.accountName)}`
    : null;

  return (
    <div className="pm-card">
      <h1>Thanh toán đơn #{payment.orderId}</h1>
      <div className="pm-amount">{formatPrice(payment.amount)}</div>

      {payment.status === 'PAID' && (
        <>
          <p className="pm-success">✔ Thanh toán thành công</p>
          <p className="pm-muted">
            Mã giao dịch: <span className="pm-copy">{payment.transactionId}</span>
          </p>
          <p>Đơn hàng đã được xác nhận và sẽ sớm được giao.</p>
        </>
      )}

      {payment.status === 'CANCELLED' && <p className="pm-error">Bạn đã huỷ thanh toán này.</p>}

      {payment.status === 'PENDING' && payment.paymentMethod === 'COD' && (
        <p>Đơn hàng thanh toán khi nhận hàng (COD). Bạn không cần thanh toán trước.</p>
      )}

      {payment.status === 'PENDING' && bank && (
        <>
          <div className="pm-grid">
            {qrUrl && !qrBroken ? (
              <img className="pm-qr" src={qrUrl} alt="Mã QR chuyển khoản" onError={() => setQrBroken(true)} />
            ) : (
              <div className="pm-qr" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                QR không tải được
              </div>
            )}
            <div className="pm-info">
              <p>Ngân hàng: <strong>{bank.bankName}</strong></p>
              <p>Số tài khoản: <span className="pm-copy">{bank.accountNo}</span></p>
              <p>Chủ tài khoản: <strong>{bank.accountName}</strong></p>
              <p>Số tiền: <strong>{formatPrice(payment.amount)}</strong></p>
              <p>Nội dung: <span className="pm-copy">{bank.transferContent}</span></p>
            </div>
          </div>
          <p className="pm-muted">
            Môi trường demo: bấm "Tôi đã chuyển khoản" để giả lập ngân hàng xác nhận giao dịch.
          </p>
          <div className="pm-actions">
            <button
              className="pm-btn pm-btn-primary"
              disabled={busy}
              onClick={() => run(() => confirmPayment(payment.id))}
            >
              {busy ? 'Đang xác nhận...' : 'Tôi đã chuyển khoản'}
            </button>
            <button className="pm-btn" disabled={busy} onClick={() => run(() => cancelPayment(payment.id))}>
              Huỷ thanh toán
            </button>
          </div>
        </>
      )}

      {error && <p className="pm-error">{error}</p>}

      <div className="pm-actions">
        <Link to={`/orders/${payment.orderId}`}>Xem chi tiết đơn hàng</Link>
        <Link to="/my-orders">Đơn hàng của tôi</Link>
      </div>
    </div>
  );
}
