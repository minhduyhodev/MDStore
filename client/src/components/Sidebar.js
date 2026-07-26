/**
 * src/components/Sidebar.js — Thanh điều hướng bên trái, hiển thị ở mọi trang.
 *
 * ❓ Tại sao file này KHÔNG cần "use client"?
 *    Sidebar chỉ render HTML tĩnh — không có useState, không có onClick,
 *    không cần browser API. Next.js tự render nó phía server (Server Component).
 *    Điều này tốt vì: nhanh hơn, SEO tốt hơn, không cần JS bundle gửi về browser.
 *
 * 🔗 Link và điều hướng: dùng <Link> của Next.js thay vì <a> bình thường.
 *    <Link> không reload cả trang — chỉ fetch phần content thay đổi (như SPA).
 */
import Link from 'next/link';

// Danh sách menu — thêm trang mới vào đây là đủ
const NAV_ITEMS = [
  { href: '/catalog', label: '🛍️  Sản phẩm'  },
  { href: '/orders',  label: '📦  Đơn hàng'  },
  { href: '/wallet',  label: '💰  Ví tiền'   },
];

export default function Sidebar() {
  return (
    <aside className="w-56 min-h-screen bg-gray-900 text-white flex flex-col py-6 px-4 gap-2">
      {/* Logo / tên app */}
      <div className="text-xl font-bold mb-6 px-2 text-indigo-400">MDStore</div>

      {/* Render từng mục menu */}
      {NAV_ITEMS.map((item) => (
        <Link
          key={item.href}
          href={item.href}
          className="px-3 py-2 rounded-lg text-sm text-gray-400 hover:bg-gray-800 hover:text-white transition-colors"
        >
          {item.label}
        </Link>
      ))}
    </aside>
  );
}
