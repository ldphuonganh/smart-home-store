import {
  useEffect,
  useState
} from 'react';

import {
  addWishlistItem,
  clearWishlist,
  getWishlist,
  removeWishlistItem
} from '../api/wishlistApi';

import type {
  Wishlist
} from '../types/wishlist';

export default function WishlistPage() {

  const [wishlist, setWishlist] =
    useState<Wishlist | null>(null);

  const [productId, setProductId] =
    useState('');

  const [loading, setLoading] =
    useState(true);

  const [message, setMessage] =
    useState('');

  const [error, setError] =
    useState('');

  const loadWishlist = async () => {

    try {

      setLoading(true);
      setError('');

      const response =
        await getWishlist();

      setWishlist(response.data);

    } catch (err) {

      console.error(err);

      setError(
        'Khong tai duoc danh sach yeu thich.'
      );

    } finally {

      setLoading(false);
    }
  };

  useEffect(() => {

    loadWishlist();

  }, []);

  const handleAddItem = async () => {

    const productIdNumber =
      Number(productId);

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

    try {

      setError('');
      setMessage('');

      const response =
        await addWishlistItem(
          productIdNumber
        );

      setWishlist(response.data);

      setProductId('');

      setMessage(
        'Da them san pham vao danh sach yeu thich.'
      );

    } catch (err) {

      console.error(err);

      setError(
        'Them san pham vao danh sach yeu thich that bai.'
      );
    }
  };

  const handleRemove =
    async (id: number) => {

      try {

        setError('');
        setMessage('');

        await removeWishlistItem(id);

        await loadWishlist();

        setMessage(
          'Da xoa san pham khoi danh sach yeu thich.'
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
          'Ban co chac muon xoa toan bo danh sach yeu thich?'
        )
      ) {
        return;
      }

      try {

        setError('');
        setMessage('');

        await clearWishlist();

        await loadWishlist();

        setMessage(
          'Da xoa toan bo danh sach yeu thich.'
        );

      } catch (err) {

        console.error(err);

        setError(
          'Xoa danh sach yeu thich that bai.'
        );
      }
    };

  if (loading) {

    return (
      <div style={{ padding: 24 }}>
        Dang tai danh sach yeu thich...
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

      <h1>
        Danh sach yeu thich
      </h1>

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
            flexWrap: 'wrap'
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

          <button
            onClick={handleAddItem}
          >
            Them vao yeu thich
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

      {!wishlist ||
      wishlist.items.length === 0 ? (

        <p>
          Danh sach yeu thich dang trong.
        </p>

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
              San pham yeu thich (
              {wishlist.items.length})
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

            {wishlist.items.map((item) => (

              <div
                key={item.id}
                style={{
                  border: '1px solid #ddd',
                  borderRadius: 8,
                  padding: 16,
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}
              >

                <div>

                  <strong>
                    Product ID: {item.productId}
                  </strong>

                </div>

                <button
                  onClick={() =>
                    handleRemove(item.id)
                  }
                >
                  Xoa
                </button>

              </div>

            ))}

          </div>

        </div>
      )}

    </div>
  );
}