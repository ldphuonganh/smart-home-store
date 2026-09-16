import {
  Link,
  useLocation
} from 'react-router-dom';

import type {
  Order
} from '../types/order';

interface OrderSuccessState {
  order?: Order;
}

export default function OrderSuccessPage() {

  const location = useLocation();

  const state =
    location.state as OrderSuccessState | null;

  const order = state?.order;

  const formatMoney = (
    value: number
  ) => {
    return new Intl.NumberFormat(
      'vi-VN'
    ).format(value);
  };

  return (
    <div
      style={{
        maxWidth: 700,
        margin: '50px auto',
        padding: 30,
        textAlign: 'center',
        border: '1px solid #ddd',
        borderRadius: 10
      }}
    >

      <div
        style={{
          fontSize: 60,
          marginBottom: 20
        }}
      >
        ✓
      </div>

      <h1
        style={{
          fontSize: 32
        }}
      >
        Đặt hàng thành công
      </h1>

      <p
        style={{
          marginBottom: 25
        }}
      >
        Cảm ơn bạn đã đặt hàng tại SmartHome.
      </p>

      {order && (
        <div
          style={{
            textAlign: 'left',
            background: '#f8f9fa',
            padding: 20,
            borderRadius: 8,
            marginBottom: 25
          }}
        >

          <p>
            <strong>
              Mã đơn hàng:
            </strong>
            {' '}
            #{order.id}
          </p>

          <p>
            <strong>
              Trạng thái:
            </strong>
            {' '}
            {order.status}
          </p>

          <p>
            <strong>
              Phương thức thanh toán:
            </strong>
            {' '}
            {order.paymentMethod}
          </p>

          <p>
            <strong>
              Tổng tiền:
            </strong>
            {' '}
            {formatMoney(
              order.totalAmount
            )}
            {' '}₫
          </p>

        </div>
      )}

      <div
        style={{
          display: 'flex',
          gap: 10,
          justifyContent: 'center'
        }}
      >

        {order && (
          <Link
            to={`/orders/${order.id}`}
            style={{
              padding: '10px 16px',
              borderRadius: 6,
              background: '#0d6efd',
              color: 'white',
              textDecoration: 'none'
            }}
          >
            Xem chi tiết đơn hàng
          </Link>
        )}

        <Link
          to="/my-orders"
          style={{
            padding: '10px 16px',
            borderRadius: 6,
            border: '1px solid #999',
            textDecoration: 'none'
          }}
        >
          Xem đơn hàng của tôi
        </Link>

      </div>

    </div>
  );
}