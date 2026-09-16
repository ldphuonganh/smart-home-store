import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

import { login as loginApi } from '../api/authApi';
import { useAuth } from '../context/AuthContext';
import type { ApiErrorResponse } from '../types/apiError';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    setError(null);
    setSubmitting(true);

    try {
      const response = await loginApi({
        username,
        password
      });

      // Lưu thông tin đăng nhập và JWT
      login(response.data);

      // Sau khi đăng nhập, chuyển đến trang đơn hàng
      navigate('/my-orders');
    } catch (err) {
      if (
        axios.isAxiosError<ApiErrorResponse>(err) &&
        err.response?.data?.message
      ) {
        setError(err.response.data.message);
      } else {
        setError('Đăng nhập thất bại, vui lòng thử lại.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      style={{
        maxWidth: 360,
        margin: '80px auto',
        padding: 24,
        border: '1px solid #ddd',
        borderRadius: 8,
        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)'
      }}
    >
      <h2 style={{ textAlign: 'center' }}>
        Đăng nhập SmartHome
      </h2>

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: 12 }}>
          <label htmlFor="username">
            Tên đăng nhập
          </label>

          <input
            id="username"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            style={{
              width: '100%',
              boxSizing: 'border-box',
              padding: 8,
              marginTop: 5
            }}
          />
        </div>

        <div style={{ marginBottom: 12 }}>
          <label htmlFor="password">
            Mật khẩu
          </label>

          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            style={{
              width: '100%',
              boxSizing: 'border-box',
              padding: 8,
              marginTop: 5
            }}
          />
        </div>

        {error && (
          <p style={{ color: '#b91c1c' }}>
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={submitting}
          style={{
            width: '100%',
            padding: 10,
            cursor: submitting ? 'not-allowed' : 'pointer'
          }}
        >
          {submitting ? 'Đang xử lý...' : 'Đăng nhập'}
        </button>
      </form>
    </div>
  );
}