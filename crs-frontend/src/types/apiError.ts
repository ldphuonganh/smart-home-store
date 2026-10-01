/**
 * Format lỗi thống nhất của cả nhóm:
 * { timestamp, status, error, message, path }
 * Lỗi validation có thêm errors: { tenField: thongBao }.
 * (Giữ index signature để tương thích với code cũ đọc lỗi dạng { tenField: "..." }.)
 */
export interface ApiErrorResponse {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  errors?: Record<string, string>;
  [field: string]: unknown;
}
