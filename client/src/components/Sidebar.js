'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Package, ShoppingBag, Wallet, Box, Server } from 'lucide-react';
import { useStore } from '@/store/useStore';
import { useEffect, useState } from 'react';

const NAV_ITEMS = [
  { href: '/catalog', label: 'Sản phẩm', icon: Package },
  { href: '/orders',  label: 'Đơn hàng', icon: ShoppingBag },
  { href: '/wallet',  label: 'Ví tiền', icon: Wallet },
];

const ADMIN_ITEMS = [
  { href: '/admin/suppliers', label: 'Nguồn cung cấp', icon: Server },
];

export default function Sidebar() {
  const pathname = usePathname();
  const { user } = useStore();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setMounted(true);
  }, []);

  const renderNavItems = (items) => {
    return items.map((item) => {
      const isActive = pathname === item.href || pathname?.startsWith(item.href + '/');
      const Icon = item.icon;
      
      return (
        <Link
          key={item.href}
          href={item.href}
          className={`
            group flex items-center gap-3 px-4 py-3 rounded-2xl transition-all duration-300
            ${isActive 
              ? 'bg-emerald-50 text-emerald-700 font-bold' 
              : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
            }
          `}
        >
          <Icon 
            size={20} 
            className={`transition-transform duration-300 ${isActive ? 'scale-110 text-emerald-600' : 'text-slate-400 group-hover:scale-110 group-hover:text-slate-600'}`} 
            strokeWidth={isActive ? 2.5 : 2} 
          />
          {item.label}
          
          {isActive && (
            <div className="ml-auto w-1.5 h-1.5 rounded-full bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.8)]" />
          )}
        </Link>
      );
    });
  };

  return (
    <aside className="w-64 min-h-screen border-r border-slate-200 bg-white flex flex-col py-8 px-4 gap-6 sticky top-0 h-screen overflow-y-auto">
      {/* Logo */}
      <div className="flex items-center gap-3 px-4 mb-2">
        <div className="w-10 h-10 rounded-xl bg-emerald-600 flex items-center justify-center text-white shadow-sm shadow-emerald-600/30">
          <Box size={24} strokeWidth={2.5} />
        </div>
        <div className="font-bold text-2xl tracking-tight text-slate-900">
          MDStore
        </div>
      </div>

      {/* Render conditional navigation based on role */}
      {mounted && user?.role === 'ADMIN' ? (
        <nav className="flex flex-col gap-1.5">
          <div className="px-4 text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">
            Quản trị hệ thống
          </div>
          {renderNavItems(ADMIN_ITEMS)}
        </nav>
      ) : (
        <nav className="flex flex-col gap-1.5">
          <div className="px-4 text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">
            Giao dịch
          </div>
          {renderNavItems(NAV_ITEMS)}
        </nav>
      )}

      {/* Footer Info */}
      <div className="mt-auto px-4 py-4 rounded-2xl bg-slate-50 border border-slate-200">
        <div className="text-xs font-bold text-slate-600 mb-1">Phiên bản 1.0</div>
        <div className="text-xs font-medium text-slate-500 leading-relaxed">
          Quản lý bán hàng & Tích hợp API tự động.
        </div>
      </div>
    </aside>
  );
}
