import {
  Routes,
  Route,
  Navigate
} from 'react-router-dom';

// Components
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';

// Pages
import LoginPage from './pages/LoginPage';
import CheckoutPage from './pages/CheckoutPage';
import OrderSuccessPage from './pages/OrderSuccessPage';
import MyOrdersPage from './pages/MyOrdersPage';
import OrderDetailPage from './pages/OrderDetailPage';
import AdminOrdersPage from './pages/AdminOrdersPage';

function App() {
  return (
    <>
      <Navbar />

      <Routes>
        {/* Trang mặc định */}
        <Route
          path="/"
          element={
            <Navigate
              to="/login"
              replace
            />
          }
        />

        {/* ĐĂNG NHẬP */}
        <Route
          path="/login"
          element={
            <LoginPage />
          }
        />

        {/* =========================
            TV4 - ORDER + CHECKOUT
           ========================= */}

        {/* Thanh toán / Đặt hàng */}
        <Route
          path="/checkout"
          element={
            <ProtectedRoute requiredRole="CUSTOMER">
              <CheckoutPage />
            </ProtectedRoute>
          }
        />

        {/* Đặt hàng thành công */}
        <Route
          path="/orders/:id/success"
          element={
            <ProtectedRoute requiredRole="CUSTOMER">
              <OrderSuccessPage />
            </ProtectedRoute>
          }
        />

        {/* Lịch sử đơn hàng của khách hàng */}
        <Route
          path="/my-orders"
          element={
            <ProtectedRoute requiredRole="CUSTOMER">
              <MyOrdersPage />
            </ProtectedRoute>
          }
        />

        {/* Chi tiết đơn hàng */}
        <Route
          path="/orders/:id"
          element={
            <ProtectedRoute requiredRole="CUSTOMER">
              <OrderDetailPage />
            </ProtectedRoute>
          }
        />

        {/* Quản lý đơn hàng dành cho Admin */}
        <Route
          path="/admin/orders"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <AdminOrdersPage />
            </ProtectedRoute>
          }
        />

        {/* Đường dẫn không tồn tại */}
        <Route
          path="*"
          element={
            <Navigate
              to="/login"
              replace
            />
          }
        />
      </Routes>
    </>
  );
}

export default App;