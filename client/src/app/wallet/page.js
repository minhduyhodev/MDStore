'use client';

import { useState, useEffect } from 'react';
import Header from '@/components/Header';
import { motion } from 'framer-motion';
import { Wallet as WalletIcon, ArrowUpRight, ArrowDownRight, Plus, ScanLine, Clock, CreditCard } from 'lucide-react';

const MOCK_WALLET = { balance: 1250000, userId: 'user-001', cardNumber: '**** **** **** 1234', expiry: '12/28' };

const MOCK_TRANSACTIONS = [
  { id: 't1', type: 'DEPOSIT',  amount:  500000, description: 'Nạp tiền qua MoMo',   createdAt: '2026-07-20T09:00:00Z' },
  { id: 't2', type: 'PURCHASE', amount:  -95000, description: 'Mua Netflix 1 tháng', createdAt: '2026-07-25T10:31:00Z' },
  { id: 't3', type: 'DEPOSIT',  amount: 1000000, description: 'Nạp tiền qua VNPay', createdAt: '2026-07-22T15:00:00Z' },
  { id: 't4', type: 'PURCHASE', amount: -155000, description: 'Mua Spotify (huỷ)',   createdAt: '2026-07-23T11:00:00Z' },
];

const containerVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.5, ease: 'easeOut' } }
};

export default function WalletPage() {
  const [wallet, setWallet] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setWallet(MOCK_WALLET);
      setTransactions(MOCK_TRANSACTIONS);
      setLoading(false);
    }, 600);
    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="flex-1 flex flex-col h-full overflow-hidden">
      <Header title="Ví điện tử" />
      
      <main className="flex-1 overflow-y-auto p-8">
        <div className="max-w-5xl mx-auto space-y-8">
          
          {loading ? (
            <div className="flex flex-col lg:flex-row gap-8 animate-pulse">
              <div className="w-full lg:w-96 h-56 bg-slate-200 dark:bg-slate-800 rounded-3xl"></div>
              <div className="flex-1 h-96 bg-slate-200 dark:bg-slate-800 rounded-3xl"></div>
            </div>
          ) : (
            <motion.div 
              variants={containerVariants}
              initial="hidden"
              animate="visible"
              className="flex flex-col lg:flex-row gap-8 items-start"
            >
              {/* Credit Card / Balance */}
              <div className="w-full lg:w-[400px] shrink-0 space-y-6">
                <div className="relative h-56 rounded-3xl p-6 text-white overflow-hidden shadow-2xl shadow-indigo-500/30 bg-gradient-to-tr from-slate-900 via-indigo-900 to-purple-800">
                  {/* Decorative Elements */}
                  <div className="absolute top-0 right-0 w-64 h-64 bg-white opacity-5 rounded-full blur-3xl -translate-y-1/2 translate-x-1/3"></div>
                  <div className="absolute bottom-0 left-0 w-48 h-48 bg-indigo-500 opacity-20 rounded-full blur-2xl translate-y-1/3 -translate-x-1/3"></div>
                  
                  <div className="relative z-10 h-full flex flex-col justify-between">
                    <div className="flex justify-between items-center">
                      <div className="flex items-center gap-2 text-white/80 font-medium">
                        <WalletIcon size={20} /> Số dư khả dụng
                      </div>
                      <ScanLine size={24} className="text-white/50" />
                    </div>
                    
                    <div>
                      <div className="text-4xl font-black tracking-tight mb-1">
                        {wallet?.balance.toLocaleString('vi-VN')}₫
                      </div>
                      <div className="font-mono text-white/60 tracking-widest text-sm mb-4">
                        {wallet?.cardNumber}
                      </div>
                    </div>
                    
                    <div className="flex justify-between items-end text-sm">
                      <div>
                        <div className="text-white/50 text-xs uppercase tracking-wider mb-0.5">Chủ thẻ</div>
                        <div className="font-semibold">{wallet?.userId.toUpperCase()}</div>
                      </div>
                      <div>
                        <div className="text-white/50 text-xs uppercase tracking-wider mb-0.5">Hiệu lực</div>
                        <div className="font-semibold">{wallet?.expiry}</div>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Quick Actions */}
                <div className="grid grid-cols-2 gap-4">
                  <button className="flex flex-col items-center justify-center p-4 rounded-2xl bg-indigo-50 dark:bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 hover:bg-indigo-100 dark:hover:bg-indigo-500/20 transition-colors font-semibold">
                    <Plus size={24} className="mb-2" />
                    Nạp tiền
                  </button>
                  <button className="flex flex-col items-center justify-center p-4 rounded-2xl bg-slate-50 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors font-semibold">
                    <CreditCard size={24} className="mb-2 text-slate-500" />
                    Liên kết thẻ
                  </button>
                </div>
              </div>

              {/* Transaction History */}
              <div className="flex-1 w-full bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                <div className="px-6 py-5 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center">
                  <h2 className="text-lg font-bold text-slate-800 dark:text-white flex items-center gap-2">
                    <Clock size={20} className="text-indigo-500" />
                    Lịch sử giao dịch
                  </h2>
                  <button className="text-sm font-medium text-indigo-600 dark:text-indigo-400 hover:underline">
                    Xem tất cả
                  </button>
                </div>
                
                <div className="divide-y divide-slate-100 dark:divide-slate-800/60">
                  {transactions.map((tx) => {
                    const isDeposit = tx.amount > 0;
                    return (
                      <div key={tx.id} className="flex items-center justify-between p-6 hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition-colors group">
                        <div className="flex items-center gap-4">
                          <div className={`w-12 h-12 rounded-full flex items-center justify-center ${isDeposit ? 'bg-emerald-100 dark:bg-emerald-500/20 text-emerald-600 dark:text-emerald-400' : 'bg-rose-100 dark:bg-rose-500/20 text-rose-600 dark:text-rose-400'}`}>
                            {isDeposit ? <ArrowUpRight size={20} strokeWidth={2.5} /> : <ArrowDownRight size={20} strokeWidth={2.5} />}
                          </div>
                          <div>
                            <div className="font-semibold text-slate-800 dark:text-slate-200 mb-1 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors">
                              {tx.description}
                            </div>
                            <div className="text-sm text-slate-500 dark:text-slate-400">
                              {new Date(tx.createdAt).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })}
                            </div>
                          </div>
                        </div>
                        
                        <div className={`text-lg font-bold ${isDeposit ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-900 dark:text-white'}`}>
                          {isDeposit ? '+' : ''}{tx.amount.toLocaleString('vi-VN')}₫
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </motion.div>
          )}
        </div>
      </main>
    </div>
  );
}
