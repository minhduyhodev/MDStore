'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Header from '@/components/Header';
import { motion } from 'framer-motion';
import { CheckCircle2, Clock, AlertCircle, RefreshCw, Box, Download, Filter } from 'lucide-react';

const MOCK_ORDERS = [
  { id: 'ORD-9821', productName: 'Netflix 1 tháng', status: 'COMPLETED',            amount: 95000,  createdAt: '2026-07-25T10:30:00Z' },
  { id: 'ORD-9822', productName: 'Spotify Premium',  status: 'PENDING',              amount: 69000,  createdAt: '2026-07-25T14:00:00Z' },
  { id: 'ORD-9823', productName: 'ChatGPT Plus',     status: 'FAILED_PRICE_CHANGED', amount: 560000, createdAt: '2026-07-24T09:15:00Z' },
  { id: 'ORD-9824', productName: 'Canva Pro 1 năm',  status: 'PROCESSING_RETRY',     amount: 320000, createdAt: '2026-07-26T08:00:00Z' },
  { id: 'ORD-9825', productName: 'Youtube Premium',  status: 'COMPLETED',            amount: 45000,  createdAt: '2026-07-27T08:00:00Z' },
];

const STATUS_DISPLAY = {
  COMPLETED:            { label: 'Hoàn thành',   icon: CheckCircle2, className: 'bg-emerald-500/10 text-emerald-600  border-emerald-500/20' },
  PENDING:              { label: 'Đang xử lý',   icon: Clock,        className: 'bg-amber-500/10 text-amber-600  border-amber-500/20' },
  FAILED_PRICE_CHANGED: { label: 'Lỗi giá đổi',  icon: AlertCircle,  className: 'bg-rose-500/10 text-rose-600  border-rose-500/20' },
  PROCESSING_RETRY:     { label: 'Đang thử lại', icon: RefreshCw,    className: 'bg-indigo-500/10 text-indigo-600  border-indigo-500/20' },
};

const containerVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.5, ease: 'easeOut' } }
};

export default function OrdersPage() {
  const router = useRouter();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setOrders(MOCK_ORDERS);
      setLoading(false);
    }, 600);
    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="flex-1 flex flex-col h-full overflow-hidden">
      <Header title="Quản lý Đơn hàng" />
      
      <main className="flex-1 overflow-y-auto p-8">
        <div className="max-w-7xl mx-auto space-y-8">
          
          {/* Summary Cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-gradient-to-br from-indigo-500 to-purple-600 rounded-3xl p-6 text-white shadow-xl shadow-indigo-500/20 relative overflow-hidden">
              <div className="absolute top-0 right-0 p-6 opacity-20"><Box size={64} /></div>
              <h4 className="text-white/80 font-medium mb-1">Tổng chi tiêu</h4>
              <div className="text-4xl font-black mb-4">1.089.000₫</div>
              <div className="text-sm text-white/80 flex items-center gap-2">
                <span className="bg-white/20 px-2 py-1 rounded-md text-xs font-bold">+12%</span>
                so với tháng trước
              </div>
            </div>
            
            <div className="bg-white  border border-slate-200  rounded-3xl p-6 shadow-sm">
              <h4 className="text-slate-500  font-medium mb-1">Đơn thành công</h4>
              <div className="text-4xl font-black text-slate-800  mb-4">2</div>
              <div className="text-sm text-emerald-500 flex items-center gap-1 font-medium">Tỷ lệ hoàn thành 40%</div>
            </div>
            
            <div className="bg-white  border border-slate-200  rounded-3xl p-6 shadow-sm">
              <h4 className="text-slate-500  font-medium mb-1">Cần xử lý</h4>
              <div className="text-4xl font-black text-amber-500 mb-4">2</div>
              <div className="text-sm text-slate-400 flex items-center gap-1 font-medium">Đang trong tiến trình</div>
            </div>
          </div>

          {/* Table Toolbar */}
          <div className="flex flex-col sm:flex-row justify-between items-end sm:items-center gap-4">
            <h2 className="text-xl font-bold text-slate-800 ">Lịch sử giao dịch</h2>
            <div className="flex gap-3">
              <button className="flex items-center gap-2 px-4 py-2 bg-white  border border-slate-200  rounded-xl hover:bg-slate-50 text-sm font-medium transition-colors">
                <Filter size={16} /> Lọc
              </button>
              <button className="flex items-center gap-2 px-4 py-2 bg-indigo-50  text-indigo-600  border border-indigo-100  rounded-xl hover:bg-indigo-100 text-sm font-medium transition-colors">
                <Download size={16} /> Xuất file
              </button>
            </div>
          </div>

          {/* Table */}
          {loading ? (
             <div className="bg-white  rounded-3xl border border-slate-200  h-96 animate-pulse"></div>
          ) : (
            <motion.div 
              variants={containerVariants}
              initial="hidden"
              animate="visible"
              className="bg-white  rounded-3xl border border-slate-200  overflow-hidden shadow-sm"
            >
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-slate-50  border-b border-slate-200 ">
                      <th className="py-4 px-6 text-xs font-semibold text-slate-500 uppercase tracking-wider">Mã đơn</th>
                      <th className="py-4 px-6 text-xs font-semibold text-slate-500 uppercase tracking-wider">Sản phẩm</th>
                      <th className="py-4 px-6 text-xs font-semibold text-slate-500 uppercase tracking-wider text-right">Số tiền</th>
                      <th className="py-4 px-6 text-xs font-semibold text-slate-500 uppercase tracking-wider text-center">Trạng thái</th>
                      <th className="py-4 px-6 text-xs font-semibold text-slate-500 uppercase tracking-wider">Thời gian</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 ">
                    {orders.map((order) => {
                      const status = STATUS_DISPLAY[order.status] ?? { label: order.status, icon: Box, className: 'bg-slate-100 text-slate-600 border-slate-200' };
                      const StatusIcon = status.icon;
                      
                      return (
                        <tr 
                          key={order.id} 
                          onClick={() => router.push(`/orders/${order.id}`)}
                          className="hover:bg-slate-50/50  transition-colors group cursor-pointer"
                        >
                          <td className="py-4 px-6">
                            <span className="font-mono text-xs font-medium text-slate-400 group-hover:text-indigo-500 transition-colors">{order.id}</span>
                          </td>
                          <td className="py-4 px-6">
                            <div className="font-semibold text-slate-800 ">{order.productName}</div>
                          </td>
                          <td className="py-4 px-6 text-right">
                            <div className="font-bold text-slate-900 ">
                              {order.amount.toLocaleString('vi-VN')}₫
                            </div>
                          </td>
                          <td className="py-4 px-6 text-center">
                            <div className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border text-xs font-bold ${status.className}`}>
                              <StatusIcon size={14} strokeWidth={2.5} className={order.status === 'PROCESSING_RETRY' ? 'animate-spin' : ''} />
                              {status.label}
                            </div>
                          </td>
                          <td className="py-4 px-6">
                            <div className="text-sm text-slate-500 font-medium">
                              {new Date(order.createdAt).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })}
                            </div>
                            <div className="text-xs text-slate-400">
                              {new Date(order.createdAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </motion.div>
          )}
        </div>
      </main>
    </div>
  );
}
