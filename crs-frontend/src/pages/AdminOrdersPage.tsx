import {
  useEffect,
  useState
} from 'react';

import {
  getAllOrders,
  updateOrderStatus
} from '../api/orderApi';

import type {
  Order,
  OrderStatus
} from '../types/order';

export default function AdminOrdersPage() {

  const [orders, setOrders] =
    useState<Order[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState('');

  /*
   * Lấy tất cả đơn hàng
   */
  const loadOrders = async () => {

    try {

      setLoading(true);
      setError('');

      const response =
        await getAllOrders();

      /*
       * axiosClient trả về AxiosResponse.
       * Dữ liệu thật nằm trong response.data
       */
      setOrders(
        response.data
      );

    } catch (err: any) {

      console.error(
        'Get all orders error:',
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

  /*
   * Tải danh sách khi mở trang
   */
  useEffect(() => {

    loadOrders();

  }, []);

  /*
   * Cập nhật trạng thái đơn hàng
   */
  const handleUpdateStatus = async (
    orderId: number,
    status: OrderStatus
  ) => {

    const confirmed =
      window.confirm(
        `Bạn có chắc muốn chuyển đơn hàng #${orderId} sang "${getStatusText(status)}"?`
      );

    if (!confirmed) {
      return;
    }

    try {

      setError('');

      const response =
        await updateOrderStatus(
          orderId,
          {
            status
          }
        );

      /*
       * API trả về AxiosResponse<Order>
       * nên đơn hàng mới nằm trong response.data
       */
      const updatedOrder =
        response.data;

      /*
       * Cập nhật đơn hàng trên giao diện
       * mà không cần reload toàn bộ trang.
       */
      setOrders(
        previousOrders =>
          previousOrders.map(order =>
            order.id === updatedOrder.id
              ? updatedOrder
              : order
          )
      );

    } catch (err: any) {

      console.error(
        'Update order status error:',
        err
      );

      setError(
        err?.response?.data?.message ||
        'Không thể cập nhật trạng thái đơn hàng.'
      );
    }
  };

  /*
   * Chuyển trạng thái sang tiếng Việt
   */
  const getStatusText = (
    status: OrderStatus
  ) => {

    switch (status) {

      case 'PENDING':
        return 'Chờ xác nhận';

      case 'CONFIRMED':
        return 'Đã xác nhận';

      case 'SHIPPING':
        return 'Đang giao';

      case 'COMPLETED':
        return 'Hoàn thành';

      case 'CANCELLED':
        return 'Đã hủy';

      default:
        return status;
    }
  };

  /*
   * Format tiền Việt Nam
   */
  const formatCurrency = (
    amount: number
  ) => {

    return new Intl.NumberFormat(
      'vi-VN'
    ).format(
      Number(amount)
    ) + ' đ';
  };

  /*
   * Format ngày giờ
   */
  const formatDate = (
    date: string
  ) => {

    return new Date(date).toLocaleString(
      'vi-VN'
    );
  };

  /*
   * Các trạng thái mà Admin được phép
   * chuyển tiếp từ trạng thái hiện tại.
   */
  const getAvailableStatuses = (
    currentStatus: OrderStatus
  ): OrderStatus[] => {

    switch (currentStatus) {

      case 'PENDING':

        return [
          'CONFIRMED',
          'CANCELLED'
        ];

      case 'CONFIRMED':

        return [
          'SHIPPING',
          'CANCELLED'
        ];

      case 'SHIPPING':

        return [
          'COMPLETED'
        ];

      case 'COMPLETED':

        return [];

      case 'CANCELLED':

        return [];

      default:

        return [];
    }
  };

  /*
   * Đang tải dữ liệu
   */
  if (loading) {

    return (
      <div
        style={{
          maxWidth: '1200px',
          margin: '40px auto',
          padding: '20px'
        }}
      >

        <h2>
          Quản lý đơn hàng
        </h2>

        <p>
          Đang tải danh sách đơn hàng...
        </p>

      </div>
    );
  }

  return (
    <div
      style={{
        maxWidth: '1200px',
        margin: '40px auto',
        padding: '20px'
      }}
    >

      <h2
        style={{
          marginBottom: '20px'
        }}
      >
        Quản lý đơn hàng
      </h2>

      {/* Thông báo lỗi */}
      {error && (
        <div
          style={{
            padding: '12px',
            marginBottom: '20px',
            borderRadius: '6px',
            backgroundColor: '#ffe5e5',
            color: '#b00020',
            border: '1px solid #ffb3b3'
          }}
        >
          {error}
        </div>
      )}

      {/* Nút tải lại */}
      <button
        onClick={loadOrders}
        style={{
          marginBottom: '20px',
          padding: '10px 16px',
          cursor: 'pointer'
        }}
      >
        Tải lại
      </button>

      {/* Không có đơn hàng */}
      {orders.length === 0 ? (

        <div
          style={{
            padding: '20px',
            border: '1px solid #ddd',
            borderRadius: '6px'
          }}
        >

          <p>
            Chưa có đơn hàng nào.
          </p>

        </div>

      ) : (

        <div
          style={{
            overflowX: 'auto'
          }}
        >

          <table
            style={{
              width: '100%',
              borderCollapse: 'collapse',
              backgroundColor: '#fff'
            }}
          >

            <thead>

              <tr
                style={{
                  backgroundColor: '#f5f5f5'
                }}
              >

                <th style={thStyle}>
                  Mã đơn
                </th>

                <th style={thStyle}>
                  Khách hàng
                </th>

                <th style={thStyle}>
                  Địa chỉ
                </th>

                <th style={thStyle}>
                  Số điện thoại
                </th>

                <th style={thStyle}>
                  Thanh toán
                </th>

                <th style={thStyle}>
                  Tổng tiền
                </th>

                <th style={thStyle}>
                  Trạng thái
                </th>

                <th style={thStyle}>
                  Ngày đặt
                </th>

                <th style={thStyle}>
                  Cập nhật
                </th>

              </tr>

            </thead>

            <tbody>

              {orders.map(
                order => {

                  const availableStatuses =
                    getAvailableStatuses(
                      order.status
                    );

                  return (

                    <tr
                      key={order.id}
                    >

                      <td style={tdStyle}>
                        #{order.id}
                      </td>

                      <td style={tdStyle}>
                        Customer #{order.customerId}
                      </td>

                      <td style={tdStyle}>
                        {order.shippingAddress}
                      </td>

                      <td style={tdStyle}>
                        {order.phone}
                      </td>

                      <td style={tdStyle}>
                        {order.paymentMethod}
                      </td>

                      <td
                        style={{
                          ...tdStyle,
                          fontWeight: 'bold',
                          whiteSpace: 'nowrap'
                        }}
                      >
                        {formatCurrency(
                          order.totalAmount
                        )}
                      </td>

                      <td style={tdStyle}>

                        <span
                          style={{
                            display: 'inline-block',
                            padding: '6px 10px',
                            borderRadius: '6px',
                            backgroundColor:
                              order.status === 'COMPLETED'
                                ? '#d4edda'
                                : order.status === 'CANCELLED'
                                  ? '#f8d7da'
                                  : order.status === 'SHIPPING'
                                    ? '#cce5ff'
                                    : order.status === 'CONFIRMED'
                                      ? '#fff3cd'
                                      : '#e2e3e5',
                            color:
                              order.status === 'COMPLETED'
                                ? '#155724'
                                : order.status === 'CANCELLED'
                                  ? '#721c24'
                                  : '#333'
                          }}
                        >
                          {getStatusText(
                            order.status
                          )}
                        </span>

                      </td>

                      <td
                        style={{
                          ...tdStyle,
                          whiteSpace: 'nowrap'
                        }}
                      >
                        {formatDate(
                          order.createdAt
                        )}
                      </td>

                      <td style={tdStyle}>

                        {availableStatuses.length === 0 ? (

                          <span
                            style={{
                              color: '#777',
                              whiteSpace: 'nowrap'
                            }}
                          >
                            Không thể cập nhật
                          </span>

                        ) : (

                          <select
                            defaultValue=""
                            onChange={
                              event => {

                                const newStatus =
                                  event.target.value as OrderStatus;

                                if (!newStatus) {
                                  return;
                                }

                                handleUpdateStatus(
                                  order.id,
                                  newStatus
                                );

                                /*
                                 * Đưa select về lại
                                 * "Chọn trạng thái"
                                 */
                                event.target.value = '';
                              }
                            }
                            style={{
                              padding: '7px',
                              minWidth: '160px',
                              cursor: 'pointer'
                            }}
                          >

                            <option value="">
                              Chọn trạng thái
                            </option>

                            {availableStatuses.map(
                              status => (

                                <option
                                  key={status}
                                  value={status}
                                >
                                  {getStatusText(
                                    status
                                  )}
                                </option>

                              )
                            )}

                          </select>

                        )}

                      </td>

                    </tr>

                  );
                }
              )}

            </tbody>

          </table>

        </div>

      )}

    </div>
  );
}

/*
 * Style tiêu đề bảng
 */
const thStyle:
  React.CSSProperties = {

  border:
    '1px solid #ddd',

  padding:
    '12px',

  textAlign:
    'left',

  whiteSpace:
    'nowrap'
};

/*
 * Style ô dữ liệu
 */
const tdStyle:
  React.CSSProperties = {

  border:
    '1px solid #ddd',

  padding:
    '12px',

  verticalAlign:
    'top'
};