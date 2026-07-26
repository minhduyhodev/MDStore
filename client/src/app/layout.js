/**
 * src/app/layout.js — Layout gốc, bọc TOÀN BỘ các trang.
 *
 * Trong Next.js App Router, file layout.js đặc biệt:
 *   - Next.js tự dùng nó làm "khung ngoài" cho mọi trang con
 *   - Không cần import hay khai báo thủ công trong mỗi trang
 *   - Khi chuyển trang, layout KHÔNG bị unmount/remount — chỉ phần `children` thay đổi
 *
 * ❓ Tại sao KHÔNG cần "use client"?
 *    layout.js là Server Component — nó chỉ render HTML, không cần state hay browser event.
 *    Sidebar và Header cũng là Server Component → toàn bộ khung render phía server,
 *    nhanh và SEO-friendly.
 */
import './globals.css';
import Sidebar from '@/components/Sidebar';

export const metadata = {
  title: 'MDStore — Dropshipping tài khoản số',
  description: 'Mua và giao tài khoản số tự động qua API',
};

export default function RootLayout({ children }) {
  return (
    <html lang="vi">
      <body className="bg-gray-50">
        {/* Layout 2 cột: Sidebar cố định trái + nội dung trang phải */}
        <div className="flex min-h-screen">
          <Sidebar />

          {/* Vùng nội dung — Next.js tự inject trang hiện tại vào đây */}
          <div className="flex-1 flex flex-col">
            {children}
          </div>
        </div>
      </body>
    </html>
  );
}
