import { useState } from 'react';

import { resolveImageUrl } from '../../utils/format';

interface ProductImageProps {
  imageUrl: string | null;
  alt: string;
  className?: string;
}

/** Ảnh sản phẩm; chưa có ảnh hoặc ảnh lỗi thì hiện ô placeholder. */
export default function ProductImage({ imageUrl, alt, className = 'sp-thumb' }: ProductImageProps) {
  const src = resolveImageUrl(imageUrl);
  // Ghi nhớ đường dẫn bị lỗi; đổi ảnh mới thì tự hiển thị lại
  const [brokenSrc, setBrokenSrc] = useState<string | null>(null);

  if (!src || brokenSrc === src) {
    return (
      <div className={className} role="img" aria-label={alt}>
        🏠
      </div>
    );
  }

  return <img className={className} src={src} alt={alt} onError={() => setBrokenSrc(src)} />;
}
