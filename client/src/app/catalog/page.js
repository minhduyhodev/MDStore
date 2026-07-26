'use client';
// ☝️ PHẢI có dòng này vì trang này dùng useState và useEffect.
//
// Trong Next.js App Router, mặc định mọi component là "Server Component"
// — chạy phía server, không có state, không có sự kiện browser.
//
// Khi ta cần:
//   - useState (lưu trạng thái: danh sách, loading, lỗi)
//   - useEffect (chạy code sau khi trang xuất hiện trên browser)
//   - onClick, onChange (sự kiện từ người dùng)
// → Phải thêm "use client" để báo Next.js: "component này chạy phía browser".

import { useState, useEffect } from 'react';
import Header from '@/components/Header';

// ─── Mock data — dữ liệu giả để dựng UI trước khi có backend ─────────────────
// Khi backend sẵn sàng: xóa MOCK_PRODUCTS, uncomment đoạn apiFetch bên dưới
const MOCK_PRODUCTS = [
  { id: 'p1', name: 'Netflix 1 tháng',  category: 'Streaming', price: 85000,  isActive: true  },
  { id: 'p2', name: 'Spotify Premium',   category: 'Music',     price: 59000,  isActive: true  },
  { id: 'p3', name: 'ChatGPT Plus',      category: 'AI Tool',   price: 520000, isActive: false },
  { id: 'p4', name: 'Canva Pro 1 năm',   category: 'Design',    price: 299000, isActive: true  },
];

export default function CatalogPage() {
  // State 1: danh sách sản phẩm (mảng rỗng khi chưa tải xong)
  const [products, setProducts] = useState([]);

  // State 2: đang tải dữ liệu hay chưa → dùng để hiện spinner
  const [loading, setLoading] = useState(true);

  // State 3: thông báo lỗi nếu fetch thất bại (null = không có lỗi)
  const [error, setError] = useState(null);

  /**
   * useEffect chạy 1 lần duy nhất khi component xuất hiện trên màn hình.
   * Mảng [] rỗng ở cuối là "dependency array" — [] = chạy đúng 1 lần khi mount.
   *
   * Khi backend sẵn sàng, thay mock data bằng:
   *   import { apiFetch } from '@/lib/api';
   *   const data = await apiFetch('/api/v1/products');
   *   setProducts(data);
   */
  useEffect(() => {
    // Mô phỏng độ trễ mạng 300ms để thấy trạng thái loading hoạt động
    const timer = setTimeout(() => {
      setProducts(MOCK_PRODUCTS);
      setLoading(false);
    }, 300);

    // Cleanup: hủy timer nếu component bị unmount trước khi timer chạy xong
    return () => clearTimeout(timer);
  }, []);

  // ─── Render theo từng trạng thái ─────────────────────────────────────────

  if (loading) {
    return (
      <div className="flex-1 flex items-center justify-center text-gray-500">
        Đang tải sản phẩm...
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex-1 flex items-center justify-center text-red-500">
        Lỗi: {error}
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col">
      <Header title="Danh sách sản phẩm" />

      <main className="p-6">
        <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-4 py-3 font-semibold text-gray-600">Tên sản phẩm</th>
                <th className="text-left px-4 py-3 font-semibold text-gray-600">Danh mục</th>
                <th className="text-right px-4 py-3 font-semibold text-gray-600">Giá nhập</th>
                <th className="text-center px-4 py-3 font-semibold text-gray-600">Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id} className="border-b border-gray-100 hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium text-gray-800">{product.name}</td>
                  <td className="px-4 py-3 text-gray-500">{product.category}</td>
                  <td className="px-4 py-3 text-right text-gray-800">
                    {product.price.toLocaleString('vi-VN')}₫
                  </td>
                  <td className="px-4 py-3 text-center">
                    {product.isActive ? (
                      <span className="inline-block px-2 py-1 rounded-full text-xs bg-green-100 text-green-700 font-medium">
                        Còn hàng
                      </span>
                    ) : (
                      <span className="inline-block px-2 py-1 rounded-full text-xs bg-gray-100 text-gray-500 font-medium">
                        Hết hàng
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p className="mt-3 text-sm text-gray-400">Hiển thị {products.length} sản phẩm</p>
      </main>
    </div>
  );
}
