import {
  FormEvent,
  useState
} from 'react';

import {
  useNavigate,
  useLocation
} from 'react-router-dom';

import {
  createOrder
} from '../api/orderApi';

import type {
  OrderRequest,
  OrderItemRequest
} from '../types/order';

interface CheckoutState {
  items?: OrderItemRequest[];
}

export default function CheckoutPage() {

  const navigate = useNavigate();
  const location = useLocation();

  const state =
    location.state as CheckoutState | null;

  const [shippingAddress, setShippingAddress] =
    useState('');

  const [phone, setPhone] =
    useState('');

  const [paymentMethod, setPaymentMethod] =
    useState('COD');

  const [items, setItems] =
    useState<OrderItemRequest[]>(
      state?.items && state.items.length > 0
        ? state.items
        : [
            {
              productId: 1,
              productName: 'Smart Sofa',
              price: 12990000,
              quantity: 1
            }
          ]
    );

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState('');

  const handleQuantityChange = (
    index: number,
    quantity: number
  ) => {

    if (quantity < 1) {
      return;
    }

    setItems(
      currentItems =>
        currentItems.map(
          (item, itemIndex) =>
            itemIndex === index
              ? {
                  ...item,
                  quantity
                }
              : item
        )
    );
  };

  const calculateTotal = () => {
    return items.reduce(
      (total, item) => {
        const price =
          item.price ?? 0;

        return (
          total +
          price * item.quantity
        );
      },
      0
    );
  };

  const formatMoney = (
    value: number
  ) => {
    return new Intl.NumberFormat(
      'vi-VN'
    ).format(value);
  };

  const handleSubmit = async (
    event: FormEvent
  ) => {

    event.preventDefault();

    setError('');

    if (
      shippingAddress.trim() === ''
    ) {
      setError(
        'Vui lòng nhập địa chỉ giao hàng.'
      );
      return;
    }

    if (phone.trim() === '') {
      setError(
        'Vui lòng nhập số điện thoại.'
      );
      return;
    }

    if (items.length === 0) {
      setError(
        'Đơn hàng chưa có sản phẩm.'
      );
      return;
    }

    for (const item of items) {

      if (
        item.productId <= 0
      ) {
        setError(
          'Mã sản phẩm không hợp lệ.'
        );
        return;
      }

      if (
        item.quantity <= 0
      ) {
        setError(
          'Số lượng sản phẩm phải lớn hơn 0.'
        );
        return;
      }
    }

    const payload: OrderRequest = {
      shippingAddress:
        shippingAddress.trim(),

      phone:
        phone.trim(),

      paymentMethod,

      items
    };

    try {

      setLoading(true);

      const response =
        await createOrder(payload);

      navigate(
        `/orders/${response.data.id}/success`,
        {
          state: {
            order: response.data
          }
        }
      );

    } catch (err: any) {

      console.error(
        'Create order error:',
        err
      );

      const message =
        err?.response?.data?.message ||
        err?.response?.data ||
        'Không thể tạo đơn hàng. Vui lòng thử lại.';

      setError(
        typeof message === 'string'
          ? message
          : 'Không thể tạo đơn hàng. Vui lòng thử lại.'
      );

    } finally {
      setLoading(false);
    }
  };

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
          fontSize: 36,
          marginBottom: 30
        }}
      >
        Thanh toán
      </h1>

      {error && (
        <div
          style={{
            padding: 12,
            marginBottom: 20,
            border: '1px solid #dc3545',
            background: '#fff5f5',
            color: '#dc3545',
            borderRadius: 6
          }}
        >
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>

        {/* THÔNG TIN GIAO HÀNG */}
        <div
          style={{
            border: '1px solid #ddd',
            borderRadius: 8,
            padding: 20,
            marginBottom: 20
          }}
        >

          <h2>
            Thông tin giao hàng
          </h2>

          <div
            style={{
              marginBottom: 16
            }}
          >
            <label
              style={{
                display: 'block',
                fontWeight: 'bold',
                marginBottom: 6
              }}
            >
              Địa chỉ giao hàng
            </label>

            <input
              type="text"
              value={shippingAddress}
              onChange={(e) =>
                setShippingAddress(
                  e.target.value
                )
              }
              placeholder="Nhập địa chỉ giao hàng"
              style={{
                width: '100%',
                boxSizing: 'border-box',
                padding: 10,
                border: '1px solid #ccc',
                borderRadius: 5,
                fontSize: 16
              }}
            />
          </div>

          <div
            style={{
              marginBottom: 16
            }}
          >
            <label
              style={{
                display: 'block',
                fontWeight: 'bold',
                marginBottom: 6
              }}
            >
              Số điện thoại
            </label>

            <input
              type="text"
              value={phone}
              onChange={(e) =>
                setPhone(
                  e.target.value
                )
              }
              placeholder="Nhập số điện thoại"
              style={{
                width: '100%',
                boxSizing: 'border-box',
                padding: 10,
                border: '1px solid #ccc',
                borderRadius: 5,
                fontSize: 16
              }}
            />
          </div>

          <div>
            <label
              style={{
                display: 'block',
                fontWeight: 'bold',
                marginBottom: 6
              }}
            >
              Phương thức thanh toán
            </label>

            <select
              value={paymentMethod}
              onChange={(e) =>
                setPaymentMethod(
                  e.target.value
                )
              }
              style={{
                width: '100%',
                padding: 10,
                border: '1px solid #ccc',
                borderRadius: 5,
                fontSize: 16
              }}
            >
              <option value="COD">
                Thanh toán khi nhận hàng (COD)
              </option>

              <option value="BANK_TRANSFER">
                Chuyển khoản ngân hàng
              </option>
            </select>
          </div>

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

          {items.map(
            (item, index) => {

              const price =
                item.price ?? 0;

              const subtotal =
                price *
                item.quantity;

              return (
                <div
                  key={index}
                  style={{
                    borderBottom:
                      '1px solid #eee',
                    padding: '15px 0',
                    display: 'flex',
                    justifyContent:
                      'space-between',
                    gap: 20,
                    alignItems: 'center'
                  }}
                >

                  <div
                    style={{
                      flex: 1
                    }}
                  >
                    <strong>
                      {item.productName ||
                        `Sản phẩm #${item.productId}`}
                    </strong>

                    <div
                      style={{
                        marginTop: 5,
                        color: '#666'
                      }}
                    >
                      Mã sản phẩm:
                      {' '}
                      {item.productId}
                    </div>

                    <div
                      style={{
                        marginTop: 5
                      }}
                    >
                      Đơn giá:
                      {' '}
                      {formatMoney(price)}
                      {' '}₫
                    </div>
                  </div>

                  <div>
                    <label>
                      Số lượng:{' '}
                    </label>

                    <input
                      type="number"
                      min="1"
                      value={item.quantity}
                      onChange={(e) =>
                        handleQuantityChange(
                          index,
                          Number(
                            e.target.value
                          )
                        )
                      }
                      style={{
                        width: 70,
                        padding: 8,
                        textAlign: 'center'
                      }}
                    />
                  </div>

                  <div
                    style={{
                      minWidth: 150,
                      textAlign: 'right',
                      fontWeight: 'bold'
                    }}
                  >
                    {formatMoney(
                      subtotal
                    )}
                    {' '}₫
                  </div>

                </div>
              );
            }
          )}

        </div>

        {/* TỔNG TIỀN */}
        <div
          style={{
            border: '1px solid #ddd',
            borderRadius: 8,
            padding: 20,
            marginBottom: 20,
            textAlign: 'right'
          }}
        >

          <span
            style={{
              fontSize: 20,
              fontWeight: 'bold'
            }}
          >
            Tổng tiền:
            {' '}
            {formatMoney(
              calculateTotal()
            )}
            {' '}₫
          </span>

        </div>

        <button
          type="submit"
          disabled={loading}
          style={{
            width: '100%',
            padding: 14,
            border: 'none',
            borderRadius: 6,
            background: loading
              ? '#999'
              : '#198754',
            color: 'white',
            fontSize: 18,
            fontWeight: 'bold',
            cursor: loading
              ? 'not-allowed'
              : 'pointer'
          }}
        >
          {loading
            ? 'Đang tạo đơn hàng...'
            : 'Đặt hàng'}
        </button>

      </form>

    </div>
  );
}