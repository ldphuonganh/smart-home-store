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

// TV2 - Product + Category
import ProductListPage from './pages/products/ProductListPage';
import ProductDetailPage from './pages/products/ProductDetailPage';
import AdminProductsPage from './pages/products/AdminProductsPage';
import AdminCategoriesPage from './pages/products/AdminCategoriesPage';

function App() {
  return (
    <>
      <Navbar />

      <Routes>
        {/* Trang mặc định: danh sách sản phẩm (ai cũng xem được) */}
        <Route
          path="/"
          element={
            <Navigate
              to="/products"
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
            TV2 - PRODUCT + CATEGORY
           ========================= */}

        {/* Danh sách + tìm kiếm/lọc sản phẩm (public) */}
        <Route
          path="/products"
          element={<ProductListPage />}
        />

        {/* Chi tiết sản phẩm (public) */}
        <Route
          path="/products/:id"
          element={<ProductDetailPage />}
        />

        {/* Admin quản lý sản phẩm */}
        <Route
          path="/admin/products"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <AdminProductsPage />
            </ProtectedRoute>
          }
        />

        {/* Admin quản lý danh mục */}
        <Route
          path="/admin/categories"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <AdminCategoriesPage />
            </ProtectedRoute>
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
              to="/products"
              replace
            />
          }
        />
      </Routes>
    </>
  );
}

export default App;