import { useCallback, useEffect, useState } from 'react';

import { getProducts } from '../api/productApi';
import type { PageResponse, Product, ProductQuery } from '../types/product';
import { getErrorMessage } from '../utils/format';

/**
 * Tách logic gọi API danh sách sản phẩm khỏi component hiển thị.
 * Trả về đủ 4 trạng thái: loading / data (success, empty) / error.
 */
export function useProducts(query: ProductQuery) {
  const [data, setData] = useState<PageResponse<Product> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  // Chuỗi hoá query để useEffect chỉ chạy lại khi tham số thật sự đổi
  const queryKey = JSON.stringify(query);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    getProducts(JSON.parse(queryKey) as ProductQuery)
      .then((res) => {
        if (!cancelled) setData(res.data);
      })
      .catch((err) => {
        if (!cancelled) setError(getErrorMessage(err, 'Không tải được danh sách sản phẩm'));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    // Bỏ kết quả của request cũ nếu người dùng đổi bộ lọc nhanh
    return () => {
      cancelled = true;
    };
  }, [queryKey, reloadKey]);

  const refetch = useCallback(() => setReloadKey((k) => k + 1), []);

  return { data, loading, error, refetch };
}
