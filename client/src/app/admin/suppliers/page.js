'use client';

import { useState, useEffect } from 'react';
import Header from '@/components/Header';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, Server, Activity, RefreshCw, AlertTriangle, CheckCircle2, Wallet, Zap, X } from 'lucide-react';

// TODO: Lấy dữ liệu từ Backend (/api/admin/suppliers) khi API sẵn sàng
const MOCK_SUPPLIERS = [
  {
    id: 'SUP-001',
    name: 'VietShare',
    code: 'VIETSHARE_V1',
    balance: 2500000,
    isActive: true,
    lastSyncStatus: 'SUCCESS',
    lastSyncTime: '2026-07-26T11:50:00Z'
  },
  {
    id: 'SUP-002',
    name: 'Test Supplier',
    code: 'MOCK_TEST_V2',
    balance: 50000,
    isActive: false,
    lastSyncStatus: 'ERROR_3_TIMES',
    lastSyncTime: '2026-07-26T10:00:00Z'
  }
];

export default function SuppliersAdminPage() {
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // State for health check
  const [healthChecking, setHealthChecking] = useState(null); // id of the supplier being checked
  
  // State for toggle confirmation modal
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, supplier: null });

  useEffect(() => {
    // Simulate initial data fetch
    const timer = setTimeout(() => {
      setSuppliers(MOCK_SUPPLIERS);
      setLoading(false);
    }, 600);
    return () => clearTimeout(timer);
  }, []);

  const handleHealthCheck = (id) => {
    setHealthChecking(id);
    // TODO: Gọi API Backend proxy (VD: POST /api/admin/suppliers/{id}/health-check)
    // KHÔNG gọi trực tiếp VietShare từ FE.
    setTimeout(() => {
      alert('Kiểm tra kết nối thành công! Nguồn cung cấp hoạt động bình thường.');
      setHealthChecking(null);
    }, 1500);
  };

  const requestToggle = (supplier) => {
    if (supplier.isActive) {
      // If turning off, show confirmation modal
      setConfirmModal({ isOpen: true, supplier });
    } else {
      // If turning on, just turn it on (or could also confirm, but turning off is the critical one)
      executeToggle(supplier.id, true);
    }
  };

  const executeToggle = (id, newStatus) => {
    // TODO: Gọi API PATCH /api/admin/suppliers/{id} { isActive: newStatus }
    setSuppliers(prev => prev.map(s => s.id === id ? { ...s, isActive: newStatus } : s));
    setConfirmModal({ isOpen: false, supplier: null });
  };

  // Summary logic
  const totalBalance = suppliers.reduce((sum, s) => sum + s.balance, 0);
  const activeCount = suppliers.filter(s => s.isActive).length;

  return (
    <div className="flex-1 flex flex-col h-full overflow-hidden bg-slate-50/50">
      <Header title="Quản lý Nguồn cung cấp (Suppliers)" />
      
      <main className="flex-1 overflow-y-auto p-4 sm:p-8">
        <div className="max-w-7xl mx-auto space-y-6">
          
          {/* Summary Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div className="bg-white rounded-lg border border-slate-200 p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-slate-500 mb-1">Tổng số dư đối tác</p>
                <div className="text-2xl font-bold text-slate-900">{loading ? '...' : totalBalance.toLocaleString('vi-VN')}₫</div>
              </div>
              <div className="p-4 bg-emerald-50 rounded-full">
                <Wallet className="text-emerald-600" size={24} />
              </div>
            </div>
            
            <div className="bg-white rounded-lg border border-slate-200 p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-slate-500 mb-1">Nguồn đang hoạt động</p>
                <div className="text-2xl font-bold text-slate-900">
                  {loading ? '...' : <><span className="text-emerald-600">{activeCount}</span> / {suppliers.length}</>}
                </div>
              </div>
              <div className="p-4 bg-sky-50 rounded-full">
                <Zap className="text-sky-600" size={24} />
              </div>
            </div>
          </div>

          {/* Action Toolbar */}
          <div className="flex justify-between items-center">
            <h2 className="text-lg font-bold text-slate-900">Danh sách Tích hợp</h2>
            <button className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors shadow-sm">
              <Plus size={16} /> Thêm nguồn mới
            </button>
          </div>

          {/* Data Table */}
          <div className="bg-white rounded-lg border border-slate-200 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200">
                    <th className="py-3 px-6 text-xs font-semibold text-slate-500 uppercase tracking-wider">Nguồn / Mã tích hợp</th>
                    <th className="py-3 px-6 text-xs font-semibold text-slate-500 uppercase tracking-wider text-right">Số dư hiện tại</th>
                    <th className="py-3 px-6 text-xs font-semibold text-slate-500 uppercase tracking-wider">Trạng thái</th>
                    <th className="py-3 px-6 text-xs font-semibold text-slate-500 uppercase tracking-wider">Lần đồng bộ cuối</th>
                    <th className="py-3 px-6 text-xs font-semibold text-slate-500 uppercase tracking-wider text-right">Hành động</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {loading ? (
                    <tr>
                      <td colSpan="5" className="py-8 text-center text-slate-500">
                        <RefreshCw className="animate-spin mx-auto mb-2" size={24} />
                        Đang tải dữ liệu...
                      </td>
                    </tr>
                  ) : suppliers.length === 0 ? (
                    <tr>
                      <td colSpan="5" className="py-8 text-center text-slate-500">
                        Chưa có nguồn cung cấp nào.
                      </td>
                    </tr>
                  ) : (
                    suppliers.map((supplier) => (
                      <tr key={supplier.id} className="even:bg-slate-50 hover:bg-slate-100/50 transition-colors">
                        {/* Cột Tên & Mã */}
                        <td className="py-4 px-6">
                          <div className="flex items-center gap-3">
                            <div className="p-2 bg-white border border-slate-200 rounded-md">
                              <Server size={18} className="text-slate-600" />
                            </div>
                            <div>
                              <div className="font-bold text-slate-900">{supplier.name}</div>
                              <div className="text-xs font-mono text-slate-500">{supplier.code}</div>
                            </div>
                          </div>
                        </td>
                        
                        {/* Cột Số dư */}
                        <td className="py-4 px-6 text-right">
                          <div className="font-bold text-slate-900">
                            {supplier.balance.toLocaleString('vi-VN')}₫
                          </div>
                        </td>
                        
                        {/* Cột Trạng thái Toggle */}
                        <td className="py-4 px-6">
                          <button 
                            onClick={() => requestToggle(supplier)}
                            className="flex items-center gap-2 group focus:outline-none"
                          >
                            <div className={`w-10 h-5 flex items-center rounded-full p-1 transition-colors duration-300 ${supplier.isActive ? 'bg-emerald-500' : 'bg-slate-300'}`}>
                              <div className={`bg-white w-3.5 h-3.5 rounded-full shadow-sm transform transition-transform duration-300 ${supplier.isActive ? 'translate-x-5' : 'translate-x-0'}`}></div>
                            </div>
                            <span className={`text-xs font-bold ${supplier.isActive ? 'text-emerald-700' : 'text-slate-500'}`}>
                              {supplier.isActive ? 'Active' : 'Inactive'}
                            </span>
                          </button>
                        </td>
                        
                        {/* Cột Đồng bộ cuối */}
                        <td className="py-4 px-6">
                          {supplier.lastSyncStatus === 'SUCCESS' ? (
                            <div className="flex flex-col">
                              <div className="flex items-center gap-1.5 text-emerald-600 text-sm font-medium">
                                <CheckCircle2 size={14} /> Thành công
                              </div>
                              <div className="text-xs text-slate-500 mt-0.5">
                                {new Date(supplier.lastSyncTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} - {new Date(supplier.lastSyncTime).toLocaleDateString('vi-VN')}
                              </div>
                            </div>
                          ) : (
                            <div className="inline-flex items-center gap-1.5 px-2 py-1 rounded-md bg-rose-50 border border-rose-200 text-rose-700 text-xs font-bold">
                              <AlertTriangle size={14} /> 
                              {supplier.lastSyncStatus === 'ERROR_3_TIMES' ? 'Lỗi 3 lần liên tiếp' : 'Lỗi đồng bộ'}
                            </div>
                          )}
                        </td>
                        
                        {/* Cột Hành động */}
                        <td className="py-4 px-6 text-right">
                          <button
                            onClick={() => handleHealthCheck(supplier.id)}
                            disabled={healthChecking === supplier.id}
                            className="inline-flex items-center gap-2 px-3 py-1.5 bg-white border border-slate-200 hover:bg-slate-50 text-slate-700 rounded-md text-xs font-bold transition-colors disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
                          >
                            {healthChecking === supplier.id ? (
                              <RefreshCw size={14} className="animate-spin text-slate-400" />
                            ) : (
                              <Activity size={14} className="text-sky-600" />
                            )}
                            {healthChecking === supplier.id ? 'Đang kiểm tra...' : 'Kiểm tra kết nối'}
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </main>

      {/* Confirmation Modal */}
      <AnimatePresence>
        {confirmModal.isOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
            <motion.div 
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden border border-slate-200"
            >
              <div className="p-6">
                <div className="w-12 h-12 rounded-full bg-rose-100 flex items-center justify-center mb-4">
                  <AlertTriangle className="text-rose-600" size={24} />
                </div>
                <h3 className="text-lg font-bold text-slate-900 mb-2">
                  Tắt nguồn cung cấp {confirmModal.supplier?.name}?
                </h3>
                <p className="text-sm text-slate-500 mb-6 leading-relaxed">
                  Bạn đang chuẩn bị tắt nguồn cung cấp này. Điều này có thể làm gián đoạn hệ thống và các đơn hàng liên quan đến nguồn này sẽ báo lỗi (Hết hàng hoặc bảo trì). Bạn có chắc chắn muốn tiếp tục?
                </p>
                <div className="flex gap-3 justify-end">
                  <button 
                    onClick={() => setConfirmModal({ isOpen: false, supplier: null })}
                    className="px-4 py-2 text-sm font-medium text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-md transition-colors"
                  >
                    Hủy bỏ
                  </button>
                  <button 
                    onClick={() => executeToggle(confirmModal.supplier.id, false)}
                    className="px-4 py-2 text-sm font-bold text-white bg-rose-600 hover:bg-rose-700 rounded-md transition-colors shadow-sm"
                  >
                    Xác nhận tắt
                  </button>
                </div>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
