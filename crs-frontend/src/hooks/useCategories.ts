import { useCallback, useEffect, useState } from 'react';

import { getCategories } from '../api/categoryApi';
import type { Category } from '../types/product';
import { getErrorMessage } from '../utils/format';

export function useCategories() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    getCategories()
      .then((res) => {
        if (!cancelled) setCategories(res.data);
      })
      .catch((err) => {
        if (!cancelled) setError(getErrorMessage(err, 'Không tải được danh mục'));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [reloadKey]);

  const refetch = useCallback(() => setReloadKey((k) => k + 1), []);

  return { categories, loading, error, refetch };
}
