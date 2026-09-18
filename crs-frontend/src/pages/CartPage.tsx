import {
  useEffect,
  useState
} from 'react';

import {
  addCartItem,
  clearCart,
  getCart,
  removeCartItem,
  updateCartItem
} from '../api/cartApi';

import type {
  Cart
} from '../types/cart';

export default function CartPage() {

  const [cart, setCart] =
    useState<Cart | null>(null);

  const [productId, setProductId] =
    useState('');

  const [quantity, setQuantity] =
    useState('1');

  const [loading, setLoading] =
    useState(true);

  const [message, setMessage] =
    useState('');

  const [error, setError] =
    useState('');

  const loadCart = async () => {

    try {

      setLoading(true);
      setError('');

      const response =
        await getCart();

      setCart(response.data);

    } catch (err) {

      console.error(err);

      setError(
        'Khong tai duoc gio hang.'
      );

    } finally {

      setLoading(false);
    }
  };

  useEffect(() => {

    loadCart();

  }, []);

  const handleAddItem = async () => {

    const productIdNumber =
      Number(productId);

    const quantityNumber =
      Number(quantity);

    if (
      !productId ||
      !Number.isInteger(productIdNumber) ||
      productIdNumber <= 0
    ) {

      setError(
        'Product ID phai la so nguyen lon hon 0.'
      );

      return;
    }

    if (
      !quantity ||
      !Number.isInteger(quantityNumber) ||
      quantityNumber <= 0
    ) {

      setError(
        'So luong phai la so nguyen lon hon 0.'
      );

      return;
    }

    try {

      setError('');
      setMessage('');

      const response =
        await addCartItem(
          productIdNumber,
          quantityNumber
        );

      setCart(response.data);

      setProductId('');
      setQuantity('1');

      setMessage(
        'Da them san pham vao gio hang.'
      );

    } catch (err) {

      console.error(err);

      setError(
        'Them san pham vao gio hang that bai.'
      );
    }
  };

  const handleIncrease =
    async (
      id: number,
      currentQuantity: number
    ) => {

      try {

        setError('');
        setMessage('');

        const response =
          await updateCartItem(
            id,
            currentQuantity + 1
          );

        setCart(response.data);

      } catch (err) {

        console.error(err);

        setError(
          'Khong the tang so luong.'
        );
      }
    };

  const handleDecrease =
    async (
      id: number,
      currentQuantity: number
    ) => {

      if (currentQuantity <= 1) {
        return;
      }

      try {

        setError('');
        setMessage('');

        const response =
          await updateCartItem(
            id,
            currentQuantity - 1
          );

        setCart(response.data);

      } catch (err) {

        console.error(err);

        setError(
          'Khong the giam so luong.'
        );
      }
    };

  const handleRemove =
    async (id: number) => {

      try {

        setError('');
        setMessage('');

        await removeCartItem(id);

        await loadCart();

        setMessage(
          'Da xoa san pham khoi gio hang.'
        );

      } catch (err) {

        console.error(err);

        setError(
          'Xoa san pham that bai.'
        );
      }
    };

  const handleClear =
    async () => {

      if (
        !window.confirm(
          'Ban co chac muon xoa toan bo gio hang?'
        )
      ) {
        return;
      }

      try {

        setError('');
        setMessage('');

        await clearCart();

        await loadCart();

        setMessage(
          'Da xoa toan bo gio hang.'
        );

      } catch (err) {

        console.error(err);

        setError(
          'Xoa gio hang that bai.'
        );
      }
    };

  if (loading) {

    return (
      <div style={{ padding: 24 }}>
        Dang tai gio hang...
      </div>
    );
  }

  return (
    <div
      style={{
        padding: 24,
        maxWidth: 900,
        margin: '0 auto'
      }}
    >

      <h1>Gio hang</h1>

      <div
        style={{
          border: '1px solid #ddd',
          padding: 16,
          marginBottom: 24,
          borderRadius: 8
        }}
      >

        <h2>Them san pham</h2>

        <div
          style={{
            display: 'flex',
            gap: 12,
            flexWrap: 'wrap',
            alignItems: 'center'
          }}
        >

          <input
            type="number"
            min="1"
            placeholder="Product ID"
            value={productId}
            onChange={(event) =>
              setProductId(event.target.value)
            }
          />

          <input
            type="number"
            min="1"
            placeholder="So luong"
            value={quantity}
            onChange={(event) =>
              setQuantity(event.target.value)
            }
          />

          <button
            onClick={handleAddItem}
          >
            Them vao gio
          </button>

        </div>

      </div>

      {message && (
        <p
          style={{
            color: 'green'
          }}
        >
          {message}
        </p>
      )}

      {error && (
        <p
          style={{
            color: 'red'
          }}
        >
          {error}
        </p>
      )}

      {!cart ||
      cart.items.length === 0 ? (

        <div>

          <p>
            Gio hang dang trong.
          </p>

        </div>

      ) : (

        <div>

          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: 16
            }}
          >

            <h2>
              San pham trong gio (
              {cart.items.length})
            </h2>

            <button
              onClick={handleClear}
            >
              Xoa tat ca
            </button>

          </div>

          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: 12
            }}
          >

            {cart.items.map((item) => (

              <div
                key={item.id}
                style={{
                  border: '1px solid #ddd',
                  borderRadius: 8,
                  padding: 16,
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  flexWrap: 'wrap',
                  gap: 12
                }}
              >

                <div>

                  <strong>
                    Product ID: {item.productId}
                  </strong>

                  <div>
                    So luong: {item.quantity}
                  </div>

                </div>

                <div
                  style={{
                    display: 'flex',
                    gap: 8,
                    alignItems: 'center'
                  }}
                >

                  <button
                    onClick={() =>
                      handleDecrease(
                        item.id,
                        item.quantity
                      )
                    }
                    disabled={
                      item.quantity <= 1
                    }
                  >
                    -
                  </button>

                  <span>
                    {item.quantity}
                  </span>

                  <button
                    onClick={() =>
                      handleIncrease(
                        item.id,
                        item.quantity
                      )
                    }
                  >
                    +
                  </button>

                  <button
                    onClick={() =>
                      handleRemove(item.id)
                    }
                  >
                    Xoa
                  </button>

                </div>

              </div>

            ))}

          </div>

        </div>
      )}

    </div>
  );
}