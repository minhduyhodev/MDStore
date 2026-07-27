import { apiFetch } from '@/lib/api';

export const WalletService = {
  /**
   * Lấy số dư ví hiện tại
   * @returns {Promise<Object>}
   */
  getBalance: () => apiFetch('/api/v1/wallet/balance'),

  /**
   * Lấy lịch sử giao dịch ví
   * @param {number} page 
   * @param {number} size 
   * @returns {Promise<Object>}
   */
  getTransactions: (page = 1, size = 10) => apiFetch(`/api/v1/wallet/transactions?page=${page}&size=${size}`),
};
