'use client';
// ☝️ CẦN "use client" vì trang này dùng useState và useEffect để load mock data.

import { useState, useEffect } from 'react';
import Header from '@/components/Header';

// ─── Mock data ────────────────────────────────────────────────────────────────
const MOCK_WALLET = { balance: 1_250_000, userId: 'user-001' };

const MOCK_TRANSACTIONS = [
  { id: 't1', type: 'DEPOSIT',  amount:  500_000, description: 'Nạp tiền qua MoMo',   createdAt: '2026-07-20T09:00:00Z' },
  { id: 't2', type: 'PURCHASE', amount:  -95_000, description: 'Mua Netflix 1 tháng', createdAt: '2026-07-25T10:31:00Z' },
  { id: 't3', type: 'DEPOSIT',  amount: 1_000_000, description: 'Nạp tiền qua VNPay', createdAt: '2026-07-22T15:00:00Z' },
  { id: 't4', type: 'PURCHASE', amount: -155_000, description: 'Mua Spotify (huỷ)',   createdAt: '2026-07-23T11:00:00Z' },
];

export default function WalletPage() {
  const [wallet, setWallet]           = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading]         = useState(true);

  useEffect(() => {
    // TODO: thay bằng apiFetch('/api/v1/wallet') khi backend sẵn sàng
    const timer = setTimeout(() => {
      setWallet(MOCK_WALLET);
      setTransactions(MOCK_TRANSACTIONS);
      setLoading(false);
    }, 300);
    return () => clearTimeout(timer);
  }, []);

  if (loading) return <div className="flex-1 flex items-center justify-center text-gray-500">Đang tải ví...</div>;

  return (
    <div className="flex-1 flex flex-col">
      <Header title="Ví tiền" />
      <main className="p-6 flex flex-col gap-6">
        {/* Thẻ số dư */}
        <div className="bg-indigo-600 text-white rounded-xl p-6 w-72">
          <p className="text-indigo-200 text-sm mb-1">Số dư hiện tại</p>
          <p className="text-3xl font-bold">
            {wallet?.balance.toLocaleString('vi-VN')}₫
          </p>
        </div>

        {/* Lịch sử giao dịch */}
        <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
          <div className="px-4 py-3 border-b border-gray-100">
            <h2 className="font-semibold text-gray-700 text-sm">Lịch sử giao dịch</h2>
          </div>
          <ul>
            {transactions.map((tx) => (
              <li key={tx.id} className="flex items-center justify-between px-4 py-3 border-b border-gray-50 hover:bg-gray-50">
                <div>
                  <p className="text-sm font-medium text-gray-800">{tx.description}</p>
                  <p className="text-xs text-gray-400">{new Date(tx.createdAt).toLocaleString('vi-VN')}</p>
                </div>
                {/* Màu xanh = nạp tiền (dương), đỏ = chi tiêu (âm) */}
                <span className={`font-semibold text-sm ${tx.amount > 0 ? 'text-green-600' : 'text-red-500'}`}>
                  {tx.amount > 0 ? '+' : ''}{tx.amount.toLocaleString('vi-VN')}₫
                </span>
              </li>
            ))}
          </ul>
        </div>
      </main>
    </div>
  );
}
