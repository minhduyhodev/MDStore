import { apiFetch } from '@/lib/api';

export const OrderService = {
  /**
   * Lấy danh sách đơn hàng của người dùng
   * @returns {Promise<Array>}
   */
  getOrders: () => apiFetch('/api/v1/orders'),

  /**
   * Lấy chi tiết đơn hàng
   * @param {string} id 
   * @returns {Promise<Object>}
   */
  getOrderById: (id) => apiFetch(`/api/v1/orders/${id}`),

  /**
   * Tạo đơn hàng mới
   * @param {Object} data 
   * @returns {Promise<Object>}
   */
  createOrder: (data) => apiFetch('/api/v1/orders', { 
    method: 'POST', 
    body: JSON.stringify(data) 
  }),

  /**
   * Xác nhận mua với giá mới khi xảy ra lỗi PRICE_CHANGED
   * @param {string} id 
   * @returns {Promise<Object>}
   */
  confirmNewPrice: (id) => apiFetch(`/api/v1/orders/${id}/confirm-new-price`, { 
    method: 'POST' 
  }),
};
