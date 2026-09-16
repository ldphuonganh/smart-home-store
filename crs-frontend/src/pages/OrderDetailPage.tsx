import {
  useEffect,
  useState
} from 'react';

import {
  Link,
  useParams
} from 'react-router-dom';

import {
  getOrderById,
  cancelOrder
} from '../api/orderApi';

import type {
  Order
} from '../types/order';

export default function OrderDetailPage() {

  const { id } = useParams();

  const [order, setOrder] =
    useState<Order | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState('');

  const loadOrder = async () => {

    if (!id) {
      setError(
        'Không xác định được mã đơn hàng.'
      );
      setLoading(false);
      return;
    }

    try {

      setLoading(true);

      const response =
        await getOrderById(
          Number(id)
        );

      setOrder(
        response.data
      );

    } catch (err: any) {

      console.error(
        'Get order detail error:',
        err
      );

      setError(
        err?.response?.data?.message ||
        'Không thể tải chi tiết đơn hàng.'
      );

    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrder();
  }, [id]);

  const handleCancel = async () => {

    if (!order) {
      return;
    }

    const confirmed =
      window.confirm(
        'Bạn có chắc muốn hủy đơn hàng này không?'
      );

    if (!confirmed) {
      return;
    }

    try {

      const response =
        await cancelOrder(
          order.id
        );

      setOrder(
        response.data
      );

    } catch (err: any) {

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
        Đang tải...
      </div>
    );
  }

  if (error || !order) {
    return (
      <div
        style={{
          padding: 40,
          textAlign: 'center'
        }}
      >

        <h2>
          Không thể tải đơn hàng
        </h2>

        <p>
          {error}
        </p>

        <Link to="/my-orders">
          Quay lại đơn hàng của tôi
        </Link>

      </div>
    );
  }

  return (
    <div
      style={{
        maxWidth: 900,
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
        Chi tiết đơn hàng #{order.id}
      </h1>

      {/* THÔNG TIN ĐƠN */}
      <div
        style={{
          border: '1px solid #ddd',
          borderRadius: 8,
          padding: 20,
          marginBottom: 20
        }}
      >

        <h2>
          Thông tin đơn hàng
        </h2>

        <p>
          <strong>
            Trạng thái:
          </strong>
          {' '}
          {order.status}
        </p>

        <p>
          <strong>
            Ngày đặt:
          </strong>
          {' '}
          {formatDate(
            order.createdAt
          )}
        </p>

        <p>
          <strong>
            Địa chỉ giao hàng:
          </strong>
          {' '}
          {order.shippingAddress}
        </p>

        <p>
          <strong>
            Số điện thoại:
          </strong>
          {' '}
          {order.phone}
        </p>

        <p>
          <strong>
            Phương thức thanh toán:
          </strong>
          {' '}
          {order.paymentMethod}
        </p>

      </div>

      {/* SẢN PHẨM */}
      <div
        style={{
          border: '1px solid #ddd',
          borderRadius: 8,
          padding: 20,
          marginBottom: 20
        }}
      >

        <h2>
          Sản phẩm
        </h2>

        {order.items.map(
          item => (

            <div
              key={item.id}
              style={{
                display: 'flex',
                justifyContent:
                  'space-between',
                padding: '15px 0',
                borderBottom:
                  '1px solid #eee'
              }}
            >

              <div>

                <strong>
                  {item.productName}
                </strong>

                <div>
                  Số lượng:
                  {' '}
                  {item.quantity}
                </div>

              </div>

              <div
                style={{
                  textAlign: 'right'
                }}
              >

                <div>
                  Đơn giá:
                  {' '}
                  {formatMoney(
                    item.price
                  )}
                  {' '}₫
                </div>

                <strong>
                  Thành tiền:
                  {' '}
                  {formatMoney(
                    item.subtotal
                  )}
                  {' '}₫
                </strong>

              </div>

            </div>
          )
        )}

        <div
          style={{
            textAlign: 'right',
            fontSize: 22,
            fontWeight: 'bold',
            marginTop: 20
          }}
        >
          Tổng tiền:
          {' '}
          {formatMoney(
            order.totalAmount
          )}
          {' '}₫
        </div>

      </div>

      {/* BUTTON */}
      <div
        style={{
          display: 'flex',
          gap: 10
        }}
      >

        <Link
          to="/my-orders"
          style={{
            padding: '10px 16px',
            border: '1px solid #999',
            borderRadius: 5,
            textDecoration: 'none'
          }}
        >
          ← Đơn hàng của tôi
        </Link>

        {order.status ===
          'PENDING' && (
          <button
            onClick={handleCancel}
            style={{
              padding: '10px 16px',
              border:
                '1px solid #dc3545',
              borderRadius: 5,
              background: 'white',
              color: '#dc3545',
              cursor: 'pointer'
            }}
          >
            Hủy đơn hàng
          </button>
        )}

      </div>

    </div>
  );
}