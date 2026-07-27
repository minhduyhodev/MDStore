import { apiFetch } from '@/lib/api';

export const CatalogService = {
  /**
   * Lấy danh sách sản phẩm
   * @returns {Promise<Array>}
   */
  getProducts: () => apiFetch('/api/v1/catalog'),

  /**
   * Lấy thông tin chi tiết một sản phẩm
   * @param {string} id 
   * @returns {Promise<Object>}
   */
  getProductById: (id) => apiFetch(`/api/v1/catalog/${id}`),
};
