'use client';

import { useState, useEffect } from 'react';
import Header from '@/components/Header';
import { motion } from 'framer-motion';
import { CheckCircle2, Clock, AlertCircle, RefreshCw, ArrowLeft, Copy, Box, Check } from 'lucide-react';
import Link from 'next/link';

// Mock data (sẽ lấy từ API sau)
const MOCK_ORDER_DETAIL = {
  'ORD-9821': {
    id: 'ORD-9821',
    productName: 'Netflix 1 tháng',
    status: 'COMPLETED',
    amount: 95000,
    createdAt: '2026-07-25T10:30:00Z',
    deliveredAccounts: [
      { id: 1, type: 'EMAIL_PASS', data: 'netflix_user1@example.com|password123' },
      { id: 2, type: 'EMAIL_PASS', data: 'netflix_user2@example.com|password456' }
    ]
  },
  'ORD-9823': {
    id: 'ORD-9823',
    productName: 'ChatGPT Plus',
    status: 'FAILED_PRICE_CHANGED',
    amount: 560000,
    newPrice: 590000, // Giá mới từ VietShare
    createdAt: '2026-07-24T09:15:00Z',
    deliveredAccounts: null
  },
  'ORD-9824': {
    id: 'ORD-9824',
    productName: 'Canva Pro 1 năm',
    status: 'PROCESSING_RETRY',
    amount: 320000,
    createdAt: '2026-07-26T08:00:00Z',
    deliveredAccounts: null
  }
};

const STATUS_DISPLAY = {
  COMPLETED:            { label: 'Hoàn thành',   icon: CheckCircle2, className: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
  PENDING:              { label: 'Đang xử lý',   icon: Clock,        className: 'bg-amber-50 text-amber-700 border-amber-200' },
  FAILED_PRICE_CHANGED: { label: 'Lỗi giá đổi',  icon: AlertCircle,  className: 'bg-rose-50 text-rose-700 border-rose-200' },
  PROCESSING_RETRY:     { label: 'Đang thử lại', icon: RefreshCw,    className: 'bg-sky-50 text-sky-700 border-sky-200' },
};

export default function OrderDetailPage({ params }) {
  const { id } = params;
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [confirmingPrice, setConfirmingPrice] = useState(false);
  const [copiedId, setCopiedId] = useState(null);

  useEffect(() => {
    // Simulate API fetch
    const timer = setTimeout(() => {
      setOrder(MOCK_ORDER_DETAIL[id] || MOCK_ORDER_DETAIL['ORD-9821']); // Fallback to ORD-9821 if not found for demo
      setLoading(false);
    }, 500);
    return () => clearTimeout(timer);
  }, [id]);

  const handleCopy = (text, accId) => {
    navigator.clipboard.writeText(text);
    setCopiedId(accId);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const handleConfirmNewPrice = () => {
    setConfirmingPrice(true);
    // Simulate API call to /api/v1/orders/{id}/confirm-new-price
    setTimeout(() => {
      alert(`Đã xác nhận mua với giá mới: ${order.newPrice?.toLocaleString('vi-VN')}₫. Hệ thống đang tạo đơn mới.`);
      setConfirmingPrice(false);
    }, 1000);
  };

  if (loading) {
    return (
      <div className="flex-1 flex flex-col h-full overflow-hidden">
        <Header title="Chi tiết Đơn hàng" />
        <main className="flex-1 p-8 flex justify-center items-center">
          <div className="animate-pulse flex flex-col items-center gap-4">
            <RefreshCw className="animate-spin text-slate-400" size={32} />
            <p className="text-slate-500 font-medium">Đang tải thông tin đơn hàng...</p>
          </div>
        </main>
      </div>
    );
  }

  const status = STATUS_DISPLAY[order.status] ?? STATUS_DISPLAY['PENDING'];
  const StatusIcon = status.icon;
  const isPriceChanged = order.status === 'FAILED_PRICE_CHANGED';

  return (
    <div className="flex-1 flex flex-col h-full overflow-hidden bg-slate-50/50">
      <Header title="Chi tiết Đơn hàng" />
      
      <main className="flex-1 overflow-y-auto p-4 sm:p-8">
        <div className="max-w-6xl mx-auto">
          
          {/* Breadcrumb & Header */}
          <div className="mb-6 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <Link 
              href="/orders" 
              className="flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-slate-900 transition-colors"
            >
              <ArrowLeft size={16} /> Quay lại danh sách
            </Link>
            
            <div className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-md border text-sm font-bold ${status.className}`}>
              <StatusIcon size={16} className={order.status === 'PROCESSING_RETRY' ? 'animate-spin' : ''} />
              {status.label}
            </div>
          </div>

          <motion.div 
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="flex flex-col lg:flex-row gap-6"
          >
            {/* Main Content Column (2/3) */}
            <div className="flex-1 space-y-6">
              
              {/* Order Info Card */}
              <div className="bg-white rounded-lg border border-slate-200 p-6">
                <div className="flex items-start gap-4">
                  <div className="p-3 bg-slate-50 rounded-lg border border-slate-100">
                    <Box size={24} className="text-slate-600" />
                  </div>
                  <div>
                    <h1 className="text-xl font-bold text-slate-900 mb-1">{order.productName}</h1>
                    <div className="flex gap-4 text-sm text-slate-500 font-medium">
                      <span>Mã: <span className="text-slate-900">{order.id}</span></span>
                      <span>•</span>
                      <span>{new Date(order.createdAt).toLocaleString('vi-VN')}</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Action Banner for PRICE_CHANGED */}
              {isPriceChanged && (
                <div className="bg-amber-50 border border-amber-200 rounded-lg p-6">
                  <div className="flex gap-3">
                    <AlertCircle className="text-amber-600 shrink-0 mt-0.5" size={20} />
                    <div>
                      <h3 className="text-amber-800 font-bold mb-2">Giá sản phẩm đã thay đổi!</h3>
                      <p className="text-amber-700 text-sm mb-4">
                        Nhà cung cấp đã thay đổi giá sản phẩm này trước khi đơn hàng được xử lý. 
                        Giá cũ: <span className="line-through">{order.amount.toLocaleString('vi-VN')}₫</span> 
                        <span className="font-bold text-rose-600 ml-2">{order.newPrice?.toLocaleString('vi-VN')}₫</span>
                      </p>
                      <button 
                        onClick={handleConfirmNewPrice}
                        disabled={confirmingPrice}
                        className="bg-emerald-600 hover:bg-emerald-700 text-white px-5 py-2.5 rounded-md text-sm font-bold transition-colors disabled:opacity-70 flex items-center gap-2"
                      >
                        {confirmingPrice ? <RefreshCw className="animate-spin" size={16} /> : <Check size={16} />}
                        Xác nhận mua giá mới
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {/* Delivered Accounts Section */}
              {order.status === 'COMPLETED' && order.deliveredAccounts && (
                <div className="bg-white rounded-lg border border-slate-200 overflow-hidden">
                  <div className="border-b border-slate-200 bg-slate-50 px-6 py-4">
                    <h3 className="font-bold text-slate-900">Tài khoản trả về</h3>
                    <p className="text-sm text-slate-500 mt-1">Lưu trữ thông tin này cẩn thận, hệ thống không cấp lại mật khẩu.</p>
                  </div>
                  <div className="p-6 space-y-3">
                    {order.deliveredAccounts.map((acc, index) => (
                      <div key={acc.id} className="flex items-center justify-between p-4 bg-slate-50 border border-slate-200 rounded-md group hover:bg-slate-100 transition-colors">
                        <div className="flex-1">
                          <div className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">Tài khoản #{index + 1} ({acc.type})</div>
                          <div className="font-mono text-sm font-medium text-slate-800">{acc.data}</div>
                        </div>
                        <button 
                          onClick={() => handleCopy(acc.data, acc.id)}
                          className="ml-4 p-2 text-slate-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-md transition-colors"
                          title="Copy dữ liệu"
                        >
                          {copiedId === acc.id ? <Check size={20} className="text-emerald-600" /> : <Copy size={20} />}
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Pending/Retry State Empty State */}
              {(order.status === 'PENDING' || order.status === 'PROCESSING_RETRY') && (
                <div className="bg-white rounded-lg border border-slate-200 p-8 text-center">
                  <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-sky-50 text-sky-600 mb-4">
                    <RefreshCw className="animate-spin" size={24} />
                  </div>
                  <h3 className="text-lg font-bold text-slate-900 mb-2">Đang xử lý đơn hàng</h3>
                  <p className="text-slate-500 text-sm max-w-sm mx-auto">
                    Hệ thống đang kết nối với nhà cung cấp. Tài khoản sẽ hiển thị tại đây ngay khi quá trình hoàn tất.
                  </p>
                </div>
              )}

            </div>

            {/* Sidebar Column (1/3) */}
            <div className="lg:w-80 shrink-0">
              <div className="bg-white rounded-lg border border-slate-200 p-6 sticky top-6">
                <h3 className="font-bold text-slate-900 mb-4">Chi tiết thanh toán</h3>
                
                <div className="space-y-3 text-sm border-b border-slate-100 pb-4 mb-4">
                  <div className="flex justify-between text-slate-600">
                    <span>Đơn giá</span>
                    <span className="font-medium">{order.amount.toLocaleString('vi-VN')}₫</span>
                  </div>
                  <div className="flex justify-between text-slate-600">
                    <span>Số lượng</span>
                    <span className="font-medium">1</span>
                  </div>
                  {isPriceChanged && (
                    <div className="flex justify-between text-rose-600 font-medium pt-2 border-t border-rose-100">
                      <span>Thay đổi giá</span>
                      <span>+{(order.newPrice - order.amount).toLocaleString('vi-VN')}₫</span>
                    </div>
                  )}
                </div>
                
                <div className="flex justify-between items-center">
                  <span className="font-bold text-slate-900">Tổng tiền</span>
                  <span className="text-xl font-bold text-emerald-600">
                    {(order.newPrice || order.amount).toLocaleString('vi-VN')}₫
                  </span>
                </div>
              </div>
            </div>

          </motion.div>
        </div>
      </main>
    </div>
  );
}
