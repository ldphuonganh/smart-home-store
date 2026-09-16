import {
  Link,
  useNavigate
} from 'react-router-dom';

import {
  useAuth
} from '../context/AuthContext';

export default function Navbar() {
  const {
    user,
    isAuthenticated,
    logout
  } = useAuth();

  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav
      style={{
        display: 'flex',
        gap: 16,
        padding: 12,
        borderBottom: '1px solid #ddd',
        alignItems: 'center',
        flexWrap: 'wrap'
      }}
    >
      {/* Trang chủ */}
      <Link to="/">
        Trang chủ
      </Link>

      {/* MENU ADMIN */}
      {isAuthenticated &&
        user?.role === 'ADMIN' && (
          <Link to="/admin/orders">
            Quản lý đơn hàng
          </Link>
        )}

      {/* MENU CUSTOMER */}
      {isAuthenticated &&
        user?.role === 'CUSTOMER' && (
          <>
            <Link to="/checkout">
              Thanh toán
            </Link>

            <Link to="/my-orders">
              Đơn hàng của tôi
            </Link>
          </>
        )}

      {/* KHU VỰC TÀI KHOẢN */}
      <div
        style={{
          marginLeft: 'auto'
        }}
      >
        {isAuthenticated ? (
          <>
            <span
              style={{
                marginRight: 12
              }}
            >
              Xin chào,{' '}
              {user?.username}{' '}
              ({user?.role})
            </span>

            <button
              onClick={handleLogout}
            >
              Đăng xuất
            </button>
          </>
        ) : (
          <Link to="/login">
            Đăng nhập
          </Link>
        )}
      </div>
    </nav>
  );
}