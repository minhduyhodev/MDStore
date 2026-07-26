/**
 * src/components/Header.js — Tiêu đề trang, nhận prop `title`.
 *
 * ❓ Tại sao file này KHÔNG cần "use client"?
 *    Header chỉ hiển thị text — không có state, không có sự kiện browser.
 *    Server Component là đủ và tốt hơn.
 */
export default function Header({ title }) {
  return (
    <header className="bg-white border-b border-gray-200 px-6 py-4">
      <h1 className="text-lg font-semibold text-gray-800">{title}</h1>
    </header>
  );
}
