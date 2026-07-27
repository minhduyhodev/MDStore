'use client';

import { useStore } from '@/store/useStore';
import { useRouter } from 'next/navigation';
import { LogOut, User, Mail, Shield, Wallet } from 'lucide-react';
import { useEffect, useState } from 'react';
import Header from '@/components/Header';

export default function ProfilePage() {
  const { user, walletBalance, logout } = useStore();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setMounted(true);
    if (!user) {
      router.replace('/login');
    }
  }, [user, router]);

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  if (!mounted || !user) return null;

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Header title="Hồ Sơ Cá Nhân" />
      
      <main className="flex-1 max-w-3xl w-full mx-auto p-8">
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          {/* Cover Photo */}
          <div className="h-32 bg-gradient-to-r from-indigo-500 to-purple-500"></div>
          
          <div className="px-8 pb-8">
            <div className="relative flex justify-between items-end -mt-12 mb-6">
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img 
                src={user.avatar} 
                alt="Avatar" 
                className="w-24 h-24 rounded-full border-4 border-white shadow-lg bg-white"
              />
              <button 
                onClick={handleLogout}
                className="flex items-center gap-2 px-4 py-2 bg-rose-50 text-rose-600 hover:bg-rose-100 rounded-lg font-medium transition-colors border border-rose-100"
              >
                <LogOut size={16} />
                <span>Đăng xuất</span>
              </button>
            </div>

            <h2 className="text-2xl font-bold text-slate-900">{user.name}</h2>
            <div className="flex items-center gap-2 text-slate-500 mt-1 mb-8">
              {user.role === 'ADMIN' ? <Shield size={16} className="text-indigo-500" /> : <User size={16} className="text-emerald-500" />}
              <span className="font-medium">{user.role === 'ADMIN' ? 'Quản trị viên hệ thống' : 'Khách hàng thành viên'}</span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="p-4 rounded-xl border border-slate-100 bg-slate-50">
                <div className="flex items-center gap-3 text-slate-500 mb-1">
                  <Mail size={16} />
                  <span className="text-sm font-medium">Email</span>
                </div>
                <div className="font-semibold text-slate-900 ml-7">{user.email}</div>
              </div>

              <div className="p-4 rounded-xl border border-slate-100 bg-slate-50">
                <div className="flex items-center gap-3 text-slate-500 mb-1">
                  <Wallet size={16} />
                  <span className="text-sm font-medium">Số dư khả dụng</span>
                </div>
                <div className="font-bold text-indigo-600 ml-7 text-lg">{walletBalance.toLocaleString('vi-VN')}₫</div>
              </div>
            </div>
            
          </div>
        </div>
      </main>
    </div>
  );
}
