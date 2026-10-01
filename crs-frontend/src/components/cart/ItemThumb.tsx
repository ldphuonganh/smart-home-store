import { useState } from 'react';

/** Ảnh nhỏ của sản phẩm trong giỏ; lỗi / chưa có ảnh thì hiện placeholder. */
export default function ItemThumb({ imageUrl, alt }: { imageUrl: string | null; alt: string }) {
  const [broken, setBroken] = useState(false);
  if (!imageUrl || broken) {
    return <div className="ct-thumb" role="img" aria-label={alt}>🏠</div>;
  }
  const base = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '');
  const src = /^https?:\/\//i.test(imageUrl) ? imageUrl : `${base}${imageUrl}`;
  return <img className="ct-thumb" src={src} alt={alt} onError={() => setBroken(true)} />;
}
