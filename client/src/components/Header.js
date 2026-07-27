'use client';

import { Bell, User, Wallet, LogIn } from 'lucide-react';
import { useStore } from '@/store/useStore';
import Link from 'next/link';
import { useEffect, useState } from 'react';

export default function Header({ title }) {
  const { user, walletBalance } = useStore();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setMounted(true);
  }, []);

  return (
    <header className="sticky top-0 z-40 bg-white/70 backdrop-blur-md border-b border-slate-200 px-8 py-4 flex items-center justify-between shadow-sm">
      <h1 className="text-xl font-semibold bg-clip-text text-transparent bg-gradient-to-r from-slate-800 to-slate-500">
        {title}
      </h1>
      
      <div className="flex items-center gap-5">
        {/* Wallet Balance Badge (chỉ dành cho USER) */}
        {mounted && user && user.role !== 'ADMIN' && (
          <Link href="/wallet" className="hidden md:flex items-center gap-2 px-3 py-1.5 rounded-full bg-indigo-50 text-indigo-600 font-medium hover:bg-indigo-100 transition-colors">
            <Wallet size={16} />
            <span>{walletBalance.toLocaleString('vi-VN')}₫</span>
          </Link>
        )}

        {mounted && user && user.role !== 'ADMIN' && (
          <button className="relative p-2 text-slate-400 hover:text-slate-600 transition-colors rounded-full hover:bg-slate-100">
            <Bell size={20} />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-red-500 border-2 border-white"></span>
          </button>
        )}
        
        <div className="h-8 w-[1px] bg-slate-200"></div>
        
        {mounted ? (
          user ? (
            <Link href="/profile" className="flex items-center gap-3 hover:opacity-80 transition-opacity">
              <div className="text-right hidden sm:block">
                <div className="text-sm font-semibold text-slate-700">{user.name}</div>
                <div className="text-xs text-slate-500">{user.role === 'ADMIN' ? 'Admin' : 'Khách hàng'}</div>
              </div>
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img src={user.avatar} alt="Avatar" className="w-10 h-10 rounded-full shadow-md border-2 border-white" />
            </Link>
          ) : (
            <Link href="/login" className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors text-sm font-medium">
              <LogIn size={16} />
              <span>Đăng nhập</span>
            </Link>
          )
        ) : (
          <div className="w-10 h-10 rounded-full bg-slate-100 animate-pulse"></div>
        )}
      </div>
    </header>
  );
}
