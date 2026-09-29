interface PaginationProps {
  page: number; // bắt đầu từ 0, khớp tham số page của backend
  totalPages: number;
  onChange: (page: number) => void;
}

/** Component phân trang dùng chung (Buổi 6). */
export default function Pagination({ page, totalPages, onChange }: PaginationProps) {
  if (totalPages <= 1) {
    return null;
  }

  // Hiển thị tối đa 5 số trang quanh trang hiện tại
  const start = Math.max(0, Math.min(page - 2, totalPages - 5));
  const end = Math.min(totalPages, start + 5);
  const pages = Array.from({ length: end - start }, (_, i) => start + i);

  return (
    <nav className="sp-pagination" aria-label="Phân trang">
      <button className="sp-btn sp-btn-sm" disabled={page === 0} onClick={() => onChange(page - 1)}>
        ‹ Trước
      </button>
      {pages.map((p) => (
        <button
          key={p}
          className={`sp-btn sp-btn-sm ${p === page ? 'sp-page-active' : ''}`}
          onClick={() => onChange(p)}
          aria-current={p === page ? 'page' : undefined}
        >
          {p + 1}
        </button>
      ))}
      <button
        className="sp-btn sp-btn-sm"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        Sau ›
      </button>
    </nav>
  );
}
