import {
  useEffect,
  useState
} from 'react';

import {
  Link
} from 'react-router-dom';

import {
  getMyOrders,
  cancelOrder
} from '../api/orderApi';

import type {
  Order
} from '../types/order';

export default function MyOrdersPage() {

  const [orders, setOrders] =
    useState<Order[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState('');

  const loadOrders = async () => {

    try {

      setLoading(true);
      setError('');

      const response =
        await getMyOrders();

      setOrders(
        response.data
      );

    } catch (err: any) {

      console.error(
        'Get orders error:',
        err
      );

      setError(
        err?.response?.data?.message ||
        'Không thể tải danh sách đơn hàng.'
      );

    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrders();
  }, []);

  const handleCancel = async (
    id: number
  ) => {

    const confirmed =
      window.confirm(
        'Bạn có chắc muốn hủy đơn hàng này không?'
      );

    if (!confirmed) {
      return;
    }

    try {

      await cancelOrder(id);

      await loadOrders();

    } catch (err: any) {

      console.error(
        'Cancel order error:',
        err
      );

      alert(
        err?.response?.data?.message ||
        'Không thể hủy đơn hàng.'
      );
    }
  };

  const formatMoney = (
    value: number
  ) => {
    return new Intl.NumberFormat(
      'vi-VN'
    ).format(value);
  };

  const formatDate = (
    value: string
  ) => {
    if (!value) {
      return '';
    }

    return new Date(
      value
    ).toLocaleString('vi-VN');
  };

  if (loading) {
    return (
      <div
        style={{
          padding: 40,
          textAlign: 'center'
        }}
      >
        Đang tải đơn hàng...
      </div>
    );
  }

  return (
    <div
      style={{
        maxWidth: 1000,
        margin: '30px auto',
        padding: '0 20px',
        textAlign: 'left'
      }}
    >

      <h1
        style={{
          textAlign: 'center',
          fontSize: 36
        }}
      >
        Đơn hàng của tôi
      </h1>

      {error && (
        <div
          style={{
            padding: 12,
            color: '#dc3545',
            border: '1px solid #dc3545',
            borderRadius: 6,
            marginBottom: 20
          }}
        >
          {error}
        </div>
      )}

      {orders.length === 0 ? (

        <div
          style={{
            padding: 40,
            textAlign: 'center',
            border: '1px solid #ddd',
            borderRadius: 8
          }}
        >
          Bạn chưa có đơn hàng nào.

          <div
            style={{
              marginTop: 20
            }}
          >
            <Link to="/courses">
              Quay lại trang chính
            </Link>
          </div>
        </div>

      ) : (

        <div>
          {orders.map(
            order => (

              <div
                key={order.id}
                style={{
                  border: '1px solid #ddd',
                  borderRadius: 8,
                  padding: 20,
                  marginBottom: 15
                }}
              >

                <div
                  style={{
                    display: 'flex',
                    justifyContent:
                      'space-between',
                    alignItems: 'center',
                    marginBottom: 12
                  }}
                >

                  <strong
                    style={{
                      fontSize: 20
                    }}
                  >
                    Đơn hàng #{order.id}
                  </strong>

                  <span
                    style={{
                      fontWeight: 'bold'
                    }}
                  >
                    {order.status}
                  </span>

                </div>

                <p>
                  Ngày đặt:
                  {' '}
                  {formatDate(
                    order.createdAt
                  )}
                </p>

                <p>
                  Phương thức:
                  {' '}
                  {order.paymentMethod}
                </p>

                <p>
                  Địa chỉ:
                  {' '}
                  {order.shippingAddress}
                </p>

                <p>
                  Tổng tiền:
                  {' '}
                  <strong>
                    {formatMoney(
                      order.totalAmount
                    )}
                    {' '}₫
                  </strong>
                </p>

                <div
                  style={{
                    display: 'flex',
                    gap: 10,
                    marginTop: 15
                  }}
                >

                  <Link
                    to={`/orders/${order.id}`}
                    style={{
                      padding: '8px 14px',
                      borderRadius: 5,
                      background: '#0d6efd',
                      color: 'white',
                      textDecoration: 'none'
                    }}
                  >
                    Chi tiết
                  </Link>

                  {order.status ===
                    'PENDING' && (
                    <button
                      onClick={() =>
                        handleCancel(
                          order.id
                        )
                      }
                      style={{
                        padding: '8px 14px',
                        borderRadius: 5,
                        border:
                          '1px solid #dc3545',
                        background: 'white',
                        color: '#dc3545',
                        cursor: 'pointer'
                      }}
                    >
                      Hủy đơn
                    </button>
                  )}

                </div>

              </div>
            )
          )}
        </div>

      )}

    </div>
  );
}