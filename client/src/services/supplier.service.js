import { apiFetch } from '@/lib/api';

export const SupplierService = {
  /**
   * Lấy danh sách các nhà cung cấp
   * @returns {Promise<Array>}
   */
  getSuppliers: () => apiFetch('/api/v1/admin/suppliers'),

  /**
   * Đồng bộ thủ công catalog từ nhà cung cấp
   * @param {string} id - ID của supplier
   * @returns {Promise<Object>}
   */
  syncCatalog: (id) => apiFetch(`/api/v1/admin/suppliers/${id}/sync`, { 
    method: 'POST' 
  }),

  /**
   * Bật/tắt nhà cung cấp (Kill switch)
   * @param {string} id - ID của supplier
   * @param {boolean} isActive - Trạng thái mong muốn
   * @returns {Promise<Object>}
   */
  toggleSupplier: (id, isActive) => apiFetch(`/api/v1/admin/suppliers/${id}/toggle`, { 
    method: 'PATCH', 
    body: JSON.stringify({ isActive }) 
  }),
};
