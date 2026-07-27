/**
 * src/lib/api.js — Hàm gọi API duy nhất của toàn bộ ứng dụng.
 *
 * Backend trả về "response envelope" chuẩn (ADR-007 trong docs/02-decisions.md):
 *   Thành công: { success: true,  data: {...},  meta: { timestamp: "..." } }
 *   Lỗi:        { success: false, error: { code: "...", message: "..." } }
 *
 * File này có 2 nhiệm vụ:
 *   1. Gọi fetch() với base URL lấy từ biến môi trường (không hardcode).
 *   2. Bóc tách envelope — component nhận thẳng phần `data`, không cần tự xử lý.
 */

// Base URL đọc từ .env.local (NEXT_PUBLIC_ prefix để Next.js expose ra browser)
// Nếu biến này bị thiếu, mọi request đều sẽ fail ngay — dễ phát hiện
const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

/**
 * Gọi một endpoint và trả về phần `data` nếu thành công.
 * Nếu backend báo lỗi (success: false) hoặc mạng lỗi → ném ra Error.
 *
 * Cách dùng trong component:
 *   const products = await apiFetch('/api/v1/products');
 *
 * @param {string} path - Đường dẫn API (bắt đầu bằng /)
 * @param {RequestInit} [options] - Tuỳ chọn fetch (method, body, headers...)
 */
export async function apiFetch(path, options = {}) {
  try {
    const response = await fetch(`${BASE_URL}${path}`, {
      headers: {
        'Content-Type': 'application/json',
        // Nếu options có headers riêng, gộp vào đây (options.headers ghi đè lên mặc định)
        ...options.headers,
      },
      ...options,
    });

    let envelope;
    try {
      // Phân tích JSON dù status code là gì (backend RESTful chuẩn luôn trả JSON dù có lỗi)
      envelope = await response.json();
    } catch (err) {
      // Xử lý trường hợp backend sập, timeout proxy trả về HTML hoặc 502/504
      const error = new Error(`Lỗi máy chủ (${response.status}): Không thể đọc dữ liệu trả về.`);
      error.status = response.status;
      throw error;
    }

    if (!response.ok || !envelope.success) {
      // Lỗi do HTTP code không phải 2xx hoặc success = false
      const message = envelope.error?.message || `Lỗi không xác định (${response.status})`;
      const error = new Error(message);
      error.code = envelope.error?.code;
      error.status = response.status;
      throw error;
    }

    // Trả về thẳng phần data — component không cần biết đến envelope
    return envelope.data;
  } catch (error) {
    // Bắt lỗi network (không thể fetch) hoặc các lỗi chủ động throw ở trên
    if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
      throw new Error('Không thể kết nối đến máy chủ. Vui lòng kiểm tra lại mạng hoặc backend.');
    }
    throw error;
  }
}
