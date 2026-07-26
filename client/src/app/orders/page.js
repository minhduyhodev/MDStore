'use client';
// ☝️ CẦN "use client" vì trang này dùng useState và useEffect để load mock data.
// Xem giải thích chi tiết về Server vs Client Component trong docs/09-frontend.md.

import { useState, useEffect } from 'react';
import Header from '@/components/Header';

// ─── Mock data ────────────────────────────────────────────────────────────────
const MOCK_ORDERS = [
  { id: 'o1', productName: 'Netflix 1 tháng', status: 'COMPLETED',            amount: 95000,  createdAt: '2026-07-25T10:30:00Z' },
  { id: 'o2', productName: 'Spotify Premium',  status: 'PENDING',              amount: 69000,  createdAt: '2026-07-25T14:00:00Z' },
  { id: 'o3', productName: 'ChatGPT Plus',     status: 'FAILED_PRICE_CHANGED', amount: 560000, createdAt: '2026-07-24T09:15:00Z' },
  { id: 'o4', productName: 'Canva Pro 1 năm',  status: 'PROCESSING_RETRY',     amount: 320000, createdAt: '2026-07-26T08:00:00Z' },
];

// Map từ status code → label tiếng Việt và màu CSS
const STATUS_DISPLAY = {
  COMPLETED:            { label: 'Hoàn thành',   className: 'bg-green-100 text-green-700'   },
  PENDING:              { label: 'Đang xử lý',   className: 'bg-yellow-100 text-yellow-700' },
  FAILED_PRICE_CHANGED: { label: 'Lỗi giá đổi', className: 'bg-red-100 text-red-700'       },
  PROCESSING_RETRY:     { label: 'Đang thử lại', className: 'bg-blue-100 text-blue-700'    },
};

export default function OrdersPage() {
  const [orders, setOrders]   = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState(null);

  useEffect(() => {
    // TODO: thay bằng apiFetch('/api/v1/orders') khi backend sẵn sàng
    const timer = setTimeout(() => {
      setOrders(MOCK_ORDERS);
      setLoading(false);
    }, 300);
    return () => clearTimeout(timer);
  }, []);

  if (loading) return <div className="flex-1 flex items-center justify-center text-gray-500">Đang tải đơn hàng...</div>;
  if (error)   return <div className="flex-1 flex items-center justify-center text-red-500">Lỗi: {error}</div>;

  return (
    <div className="flex-1 flex flex-col">
      <Header title="Lịch sử đơn hàng" />
      <main className="p-6">
        <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-4 py-3 font-semibold text-gray-600">Mã đơn</th>
                <th className="text-left px-4 py-3 font-semibold text-gray-600">Sản phẩm</th>
                <th className="text-right px-4 py-3 font-semibold text-gray-600">Số tiền</th>
                <th className="text-center px-4 py-3 font-semibold text-gray-600">Trạng thái</th>
                <th className="text-left px-4 py-3 font-semibold text-gray-600">Thời gian</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => {
                const { label, className } = STATUS_DISPLAY[order.status] ?? { label: order.status, className: 'bg-gray-100 text-gray-600' };
                return (
                  <tr key={order.id} className="border-b border-gray-100 hover:bg-gray-50">
                    <td className="px-4 py-3 font-mono text-gray-400 text-xs">{order.id}</td>
                    <td className="px-4 py-3 font-medium text-gray-800">{order.productName}</td>
                    <td className="px-4 py-3 text-right text-gray-800">{order.amount.toLocaleString('vi-VN')}₫</td>
                    <td className="px-4 py-3 text-center">
                      <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${className}`}>{label}</span>
                    </td>
                    <td className="px-4 py-3 text-gray-400 text-xs">{new Date(order.createdAt).toLocaleString('vi-VN')}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
        <p className="mt-3 text-sm text-gray-400">{orders.length} đơn hàng</p>
      </main>
    </div>
  );
}
